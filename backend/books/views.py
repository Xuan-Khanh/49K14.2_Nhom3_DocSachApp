"""
views.py – DocSachApp
"""

import random
from django.contrib.auth import authenticate
from django.contrib.auth.models import User
from django.utils import timezone
from django.db.models import Q
from django.core.cache import cache
from django.core.mail import send_mail

from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from rest_framework.permissions import IsAuthenticated, AllowAny, IsAuthenticatedOrReadOnly
from rest_framework.authtoken.models import Token
from rest_framework.parsers import MultiPartParser, FormParser, JSONParser

from .models import (
    NguoiDung, Truyen, TheLoai, TheLoaiTruyen,
    Chuong, DanhGia, BinhLuan, TheoDoiTruyen,
    TheoDoiNguoiDung, BoSuuTap, BoSuuTapTruyen, LichSuDoc
)
from .serializers import (
    DangKySerializer, DangNhapSerializer,
    NguoiDungSerializer, NguoiDungCapNhatSerializer, NguoiDungNgoanSerializer,
    TruyenSerializer, ChuongSerializer, ChuongNgoanSerializer,
    BinhLuanSerializer, DanhGiaSerializer,
    TheoDoiTruyenSerializer,
    BoSuuTapSerializer, BoSuuTapDetailSerializer,
    TheLoaiSerializer, LichSuDocSerializer
)
from .permissions import IsOwnerOrReadOnly, IsAuthorOfStory, IsCommentOwnerOrAdmin


# =============================================
# HELPER
# =============================================

def get_nguoidung_or_error(request):
    try:
        return request.user.nguoidung, None
    except:
        return None, Response({"error": "Profile not found."}, status=404)


# =============================================
# AUTH
# =============================================

class DangKyView(APIView):
    permission_classes = [AllowAny]
    def post(self, request):
        serializer = DangKySerializer(data=request.data)
        if serializer.is_valid():
            user = serializer.save()
            token, _ = Token.objects.get_or_create(user=user)
            return Response({"token": token.key, "user_id": user.nguoidung.id, "username": user.username}, status=201)
        return Response(serializer.errors, status=400)

class DangNhapView(APIView):
    permission_classes = [AllowAny]
    def post(self, request):
        username = request.data.get('username')
        password = request.data.get('password')
        user = authenticate(username=username, password=password)
        if user:
            token, _ = Token.objects.get_or_create(user=user)
            return Response({"token": token.key, "user_id": user.nguoidung.id, "username": user.username})
        return Response({"error": "Sai tài khoản hoặc mật khẩu"}, status=401)

class HoSoView(APIView):
    permission_classes = [IsAuthenticated]
    def get(self, request):
        profile, _ = get_nguoidung_or_error(request)
        return Response(NguoiDungSerializer(profile, context={'request': request}).data)
    def put(self, request):
        profile, _ = get_nguoidung_or_error(request)
        ser = NguoiDungCapNhatSerializer(profile, data=request.data, partial=True)
        if ser.is_valid():
            ser.save()
            return Response(NguoiDungSerializer(profile, context={'request': request}).data)
        return Response(ser.errors, status=400)


# =============================================
# USER
# =============================================

class UserDetailView(APIView):
    """
    GET /api/users/{id}
    Xem profile công khai của người dùng.
    """
    permission_classes = [AllowAny]

    def get(self, request, pk):
        try:
            profile = NguoiDung.objects.get(pk=pk)
        except NguoiDung.DoesNotExist:
            return Response({"error": "Không tìm thấy người dùng."}, status=404)

        serializer = NguoiDungSerializer(profile, context={'request': request})
        # Thêm danh sách truyện đã đăng
        truyen_list = profile.truyen_list.filter(trang_thai='da_dang')
        truyen_serializer = TruyenSerializer(truyen_list, many=True, context={'request': request})

        data = serializer.data
        data['truyen_da_dang'] = truyen_serializer.data

        # Add is_following flag if user is authenticated
        data['is_following'] = False
        if request.user.is_authenticated:
            try:
                data['is_following'] = TheoDoiNguoiDung.objects.filter(
                    nguoi_theo_doi=request.user.nguoidung,
                    nguoi_duoc_theo_doi=profile
                ).exists()
            except Exception:
                pass

        return Response(data)

class FollowerListView(APIView):
    """
    GET /api/users/{id}/followers
    Lấy danh sách người đang theo dõi user này
    """
    permission_classes = [AllowAny]

    def get(self, request, pk):
        try:
            profile = NguoiDung.objects.get(pk=pk)
        except NguoiDung.DoesNotExist:
            return Response({"error": "Không tìm thấy người dùng."}, status=404)
        
        followers = [td.nguoi_theo_doi for td in profile.nguoi_theo_doi_list.select_related('nguoi_theo_doi')]
        serializer = NguoiDungNgoanSerializer(followers, many=True)
        return Response(serializer.data)

class FollowingListView(APIView):
    """
    GET /api/users/{id}/following
    Lấy danh sách người mà user này đang theo dõi
    """
    permission_classes = [AllowAny]

    def get(self, request, pk):
        try:
            profile = NguoiDung.objects.get(pk=pk)
        except NguoiDung.DoesNotExist:
            return Response({"error": "Không tìm thấy người dùng."}, status=404)
        
        following = [td.nguoi_duoc_theo_doi for td in profile.dang_theo_doi.select_related('nguoi_duoc_theo_doi')]
        serializer = NguoiDungNgoanSerializer(following, many=True)
        return Response(serializer.data)


class FollowUserView(APIView):
    """
    POST /api/users/follow
    Body: {"following_id": <id người muốn follow>}
    Theo dõi người dùng khác.
    """
    permission_classes = [IsAuthenticated]

    def post(self, request):
        profile, err = get_nguoidung_or_error(request)
        if err:
            return err

        following_id = request.data.get('following_id')
        if not following_id:
            return Response({"error": "Thiếu following_id."}, status=400)

        try:
            nguoi_duoc_theo_doi = NguoiDung.objects.get(pk=following_id)
        except NguoiDung.DoesNotExist:
            return Response({"error": "Không tìm thấy người dùng."}, status=404)

        # Không cho tự follow chính mình
        if profile == nguoi_duoc_theo_doi:
            return Response({"error": "Bạn không thể tự theo dõi chính mình."}, status=400)

        # Không cho follow trùng
        if TheoDoiNguoiDung.objects.filter(nguoi_theo_doi=profile, nguoi_duoc_theo_doi=nguoi_duoc_theo_doi).exists():
            return Response({"message": "Bạn đã theo dõi người dùng này rồi."}, status=200)

        TheoDoiNguoiDung.objects.create(
            nguoi_theo_doi=profile,
            nguoi_duoc_theo_doi=nguoi_duoc_theo_doi
        )
        return Response({"message": "Đã theo dõi thành công."}, status=201)


class UnfollowUserView(APIView):
    """
    DELETE /api/users/unfollow
    Body: {"following_id": <id người muốn bỏ follow>}
    """
    permission_classes = [IsAuthenticated]

    def delete(self, request):
        profile, err = get_nguoidung_or_error(request)
        if err:
            return err

        following_id = request.data.get('following_id')
        deleted, _ = TheoDoiNguoiDung.objects.filter(
            nguoi_theo_doi=profile,
            nguoi_duoc_theo_doi_id=following_id
        ).delete()

        if deleted:
            return Response({"message": "Đã bỏ theo dõi."})
        return Response({"error": "Bạn chưa theo dõi người này."}, status=400)


# =============================================
# TRUYỆN - PHÂN LOẠI DANH MỤC
# =============================================

class TruyenMoiDangView(APIView):
    permission_classes = [AllowAny]
    def get(self, request):
        queryset = Truyen.objects.filter(trang_thai__in=['da_dang', 'hoan_thanh']).order_by('-created_at')[:20]
        serializer = TruyenSerializer(queryset, many=True, context={'request': request})
        return Response(serializer.data)

class TruyenMoiCapNhatView(APIView):
    permission_classes = [AllowAny]
    def get(self, request):
        queryset = Truyen.objects.filter(trang_thai__in=['da_dang', 'hoan_thanh']).order_by('-updated_at')[:20]
        serializer = TruyenSerializer(queryset, many=True, context={'request': request})
        return Response(serializer.data)

class TruyenHoanThanhView(APIView):
    permission_classes = [AllowAny]
    def get(self, request):
        queryset = Truyen.objects.filter(trang_thai='hoan_thanh').order_by('-updated_at')[:20]
        serializer = TruyenSerializer(queryset, many=True, context={'request': request})
        return Response(serializer.data)


# =============================================
# TRUYỆN CƠ BẢN
# =============================================

class TruyenListCreateView(APIView):
    permission_classes = [IsAuthenticatedOrReadOnly]
    parser_classes = [MultiPartParser, FormParser, JSONParser]
    def get(self, request):
        queryset = Truyen.objects.all()

        # Lọc theo người dùng (tác giả)
        nguoi_dung_id = request.query_params.get('nguoi_dung_id')
        if nguoi_dung_id:
            queryset = queryset.filter(nguoi_dung_id=nguoi_dung_id)

        # Lọc theo trạng thái. Nếu không có trạng thái và không chỉ định tác giả,
        # mặc định chỉ hiển thị truyện đã đăng (public).
        trang_thai = request.query_params.get('trang_thai')
        if trang_thai:
            queryset = queryset.filter(trang_thai=trang_thai)
        elif not nguoi_dung_id:
            queryset = queryset.filter(trang_thai__in=['da_dang', 'hoan_thanh'])

        # Lọc theo thể loại
        theloai_id = request.query_params.get('theloai')
        if theloai_id:
            queryset = queryset.filter(the_loai__id=theloai_id)

        # Tìm kiếm theo tên truyện
        search = request.query_params.get('search')
        if search: queryset = queryset.filter(ten_truyen__icontains=search)
        
        serializer = TruyenSerializer(queryset.distinct(), many=True, context={'request': request})
        return Response(serializer.data)

    def post(self, request):
        profile, err = get_nguoidung_or_error(request)
        if err:
            return err

        data_copy = request.data.copy() if hasattr(request.data, 'copy') else dict(request.data)
        if 'the_loai' in request.data:
            the_loai_list = request.data.getlist('the_loai') if hasattr(request.data, 'getlist') else request.data.get('the_loai')
            if not isinstance(the_loai_list, list):
                the_loai_list = [the_loai_list]
            if hasattr(data_copy, 'setlist'):
                data_copy.setlist('the_loai_ids', the_loai_list)
            else:
                data_copy['the_loai_ids'] = the_loai_list

        serializer = TruyenSerializer(data=data_copy, context={'request': request})
        if serializer.is_valid():
            truyen = serializer.save(nguoi_dung=profile)
            return Response(TruyenSerializer(truyen, context={'request': request}).data, status=201)
        return Response(serializer.errors, status=400)

class TruyenDetailView(APIView):
    permission_classes = [IsAuthenticatedOrReadOnly]
    
    def get_object(self, pk):
        try:
            return Truyen.objects.get(pk=pk)
        except Truyen.DoesNotExist:
            return None

    def get(self, request, pk):
        truyen = self.get_object(pk)
        if not truyen:
            return Response(status=404)
            
        truyen.so_luot_doc += 1
        truyen.save(update_fields=['so_luot_doc'])
        return Response(TruyenSerializer(truyen, context={'request': request}).data)

    def put(self, request, pk):
        truyen = self.get_object(pk)
        if not truyen:
            return Response({"error": "Không tìm thấy truyện."}, status=404)

        # Kiểm tra quyền sở hữu
        profile, err = get_nguoidung_or_error(request)
        if err:
            return err
        if truyen.nguoi_dung != profile:
            return Response({"error": "Bạn không có quyền sửa truyện này."}, status=403)

        data_copy = request.data.copy() if hasattr(request.data, 'copy') else dict(request.data)
        if 'the_loai' in request.data:
            the_loai_list = request.data.getlist('the_loai') if hasattr(request.data, 'getlist') else request.data.get('the_loai')
            if not isinstance(the_loai_list, list):
                the_loai_list = [the_loai_list]
            if hasattr(data_copy, 'setlist'):
                data_copy.setlist('the_loai_ids', the_loai_list)
            else:
                data_copy['the_loai_ids'] = the_loai_list

        serializer = TruyenSerializer(
            truyen, data=data_copy, partial=True, context={'request': request}
        )
        if serializer.is_valid():
            serializer.save()
            return Response(TruyenSerializer(truyen, context={'request': request}).data)
        return Response(serializer.errors, status=400)

    def delete(self, request, pk):
        truyen = self.get_object(pk)
        if not truyen:
            return Response({"error": "Không tìm thấy truyện."}, status=404)

        profile, err = get_nguoidung_or_error(request)
        if err:
            return err
        if truyen.nguoi_dung != profile:
            return Response({"error": "Bạn không có quyền xóa truyện này."}, status=403)
        
        truyen.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)


# =============================================
# CHƯƠNG
# =============================================

class ChuongListView(APIView):
    permission_classes = [AllowAny]
    def get(self, request, story_id):
        try:
            truyen = Truyen.objects.get(pk=story_id)
        except Truyen.DoesNotExist:
            return Response({"error": "Không tìm thấy truyện."}, status=404)

        # Public chỉ thấy chương đã đăng. Tác giả thấy tất cả.
        try:
            profile = request.user.nguoidung
            is_author = (truyen.nguoi_dung == profile)
        except Exception:
            is_author = False

        if is_author:
            chuong_list = truyen.chuong_list.all()
        else:
            chuong_list = truyen.chuong_list.filter(trang_thai='da_dang')

        serializer = ChuongNgoanSerializer(chuong_list, many=True)
        return Response(serializer.data)

class ChuongDetailView(APIView):
    permission_classes = [AllowAny]
    def get(self, request, pk):
        try:
            chuong = Chuong.objects.get(pk=pk)
            return Response(ChuongSerializer(chuong).data)
        except: return Response(status=404)


# =============================================
# BÌNH LUẬN & ĐÁNH GIÁ
# =============================================

class BinhLuanListView(APIView):
    permission_classes = [AllowAny]
    def get(self, request, story_id):
        bls = BinhLuan.objects.filter(truyen_id=story_id)
        return Response(BinhLuanSerializer(bls, many=True, context={'request': request}).data)

class DanhGiaCreateView(APIView):
    permission_classes = [IsAuthenticated]
    def post(self, request):
        profile, _ = get_nguoidung_or_error(request)
        truyen = Truyen.objects.get(pk=request.data.get('story_id'))
        DanhGia.objects.update_or_create(nguoi_dung=profile, truyen=truyen, defaults={'sao_danh_gia': request.data.get('rating')})
        return Response({"new_score": truyen.diem_trung_binh()})


# =============================================
# THEO DÕI & BỘ SƯU TẬP
# =============================================

class TheoDoiTruyenCreateView(APIView):
    permission_classes = [IsAuthenticated]
    def post(self, request):
        profile, _ = get_nguoidung_or_error(request)
        t = Truyen.objects.get(pk=request.data.get('story_id'))
        TheoDoiTruyen.objects.get_or_create(nguoi_dung=profile, truyen=t)
        return Response({"status": "followed"})

class BoSuuTapListView(APIView):
    """
    GET  /api/bosuutap  – Danh sách tất cả bộ sưu tập (public)
    """
    permission_classes = [AllowAny]

    def get(self, request):
        bst_list = BoSuuTap.objects.all()
        serializer = BoSuuTapSerializer(bst_list, many=True, context={'request': request})
        return Response(serializer.data)


class BoSuuTapListCreateView(APIView):
    permission_classes = [IsAuthenticated]
    def get(self, request):
        profile, err = get_nguoidung_or_error(request)
        if err:
            return err
        bst_list = BoSuuTap.objects.filter(nguoi_dung=profile)
        serializer = BoSuuTapSerializer(bst_list, many=True, context={'request': request})
        return Response(serializer.data)

    def post(self, request):
        profile, err = get_nguoidung_or_error(request)
        if err:
            return err
        ten = request.data.get('ten_bo_suu_tap', '').strip()
        if not ten:
            return Response({"error": "Tên bộ sưu tập không được để trống."}, status=400)
        bst = BoSuuTap.objects.create(nguoi_dung=profile, ten_bo_suu_tap=ten)
        return Response(BoSuuTapSerializer(bst).data, status=201)


class UserBoSuuTapListView(APIView):
    """
    GET /api/users/{user_id}/collections
    Lấy danh sách bộ sưu tập của một người dùng cụ thể.
    """
    permission_classes = [AllowAny]

    def get(self, request, user_id):
        try:
            profile = NguoiDung.objects.get(pk=user_id)
        except NguoiDung.DoesNotExist:
            return Response({"error": "Không tìm thấy người dùng."}, status=status.HTTP_404_NOT_FOUND)

        bst_list = BoSuuTap.objects.filter(nguoi_dung=profile)
        serializer = BoSuuTapSerializer(bst_list, many=True, context={'request': request})
        return Response(serializer.data)


class BoSuuTapDetailView(APIView):
    """
    GET    /api/collections/{id}  – Chi tiết BST kèm danh sách truyện
    PUT    /api/collections/{id}  – Đổi tên BST
    DELETE /api/collections/{id}  – Xóa BST
    """
    permission_classes = [IsAuthenticatedOrReadOnly]

    def get_object(self, pk):
        try:
            return BoSuuTap.objects.get(pk=pk)
        except BoSuuTap.DoesNotExist:
            return None

    def get(self, request, pk):
        bst = self.get_object(pk)
        if not bst:
            return Response({"error": "Không tìm thấy bộ sưu tập."}, status=404)
        serializer = BoSuuTapDetailSerializer(bst, context={'request': request})
        return Response(serializer.data)

    def put(self, request, pk):
        profile, err = get_nguoidung_or_error(request)
        if err:
            return err
        bst = self.get_object(pk)
        if not bst:
            return Response({"error": "Không tìm thấy bộ sưu tập."}, status=404)
        if bst.nguoi_dung != profile:
            return Response({"error": "Bạn không có quyền truy cập bộ sưu tập này."}, status=403)
        ten = request.data.get('ten_bo_suu_tap', '').strip()
        if not ten:
            return Response({"error": "Tên bộ sưu tập không được để trống."}, status=400)
        bst.ten_bo_suu_tap = ten
        bst.save()
        return Response(BoSuuTapSerializer(bst).data)

    def delete(self, request, pk):
        profile, err = get_nguoidung_or_error(request)
        if err:
            return err
        bst = self.get_object(pk)
        if not bst:
            return Response({"error": "Không tìm thấy bộ sưu tập."}, status=404)
        if bst.nguoi_dung != profile:
            return Response({"error": "Bạn không có quyền truy cập bộ sưu tập này."}, status=403)
        bst.delete()
        return Response({"message": "Đã xóa bộ sưu tập."})


class ThemTruyenVaoBSTView(APIView):
    """
    POST /api/collections/add-story
    Thêm truyện vào bộ sưu tập.
    Body: {collection_id, story_id}
    """
    permission_classes = [IsAuthenticated]

    def post(self, request):
        profile, err = get_nguoidung_or_error(request)
        if err:
            return err

        collection_id = request.data.get('collection_id')
        story_id = request.data.get('story_id')
        if not collection_id or not story_id:
            return Response({"error": "Thiếu collection_id hoặc story_id."}, status=400)

        try:
            bst = BoSuuTap.objects.get(pk=collection_id, nguoi_dung=profile)
        except BoSuuTap.DoesNotExist:
            return Response({"error": "Không tìm thấy bộ sưu tập."}, status=404)

        try:
            truyen = Truyen.objects.get(pk=story_id)
        except Truyen.DoesNotExist:
            return Response({"error": "Không tìm thấy truyện."}, status=404)

        if BoSuuTapTruyen.objects.filter(bo_suu_tap=bst, truyen=truyen).exists():
            return Response({"message": "Truyện đã có trong bộ sưu tập này."})

        BoSuuTapTruyen.objects.create(bo_suu_tap=bst, truyen=truyen)
        return Response({"message": "Đã thêm truyện vào bộ sưu tập."}, status=201)


class XoaTruyenKhoiBSTView(APIView):
    """
    DELETE /api/collections/remove-story
    Xóa truyện khỏi bộ sưu tập.
    Body: {collection_id, story_id}
    """
    permission_classes = [IsAuthenticated]

    def delete(self, request):
        profile, err = get_nguoidung_or_error(request)
        if err:
            return err

        collection_id = request.data.get('collection_id')
        story_id = request.data.get('story_id')

        try:
            bst = BoSuuTap.objects.get(pk=collection_id, nguoi_dung=profile)
        except BoSuuTap.DoesNotExist:
            return Response({"error": "Không tìm thấy bộ sưu tập."}, status=404)

        deleted, _ = BoSuuTapTruyen.objects.filter(
            bo_suu_tap=bst, truyen_id=story_id
        ).delete()
        if deleted:
            return Response({"message": "Đã xóa truyện khỏi bộ sưu tập."})
        return Response({"error": "Truyện không có trong bộ sưu tập."}, status=400)


class TruyenInBoSuuTapView(APIView):
    """
    GET /api/collections/{id}/stories
    Lấy danh sách truyện trong một bộ sưu tập.
    """
    permission_classes = [AllowAny]

    def get(self, request, pk):
        try:
            bst = BoSuuTap.objects.get(pk=pk)
        except BoSuuTap.DoesNotExist:
            return Response({"error": "Không tìm thấy bộ sưu tập."}, status=404)

        truyen_list = bst.truyen_list.all()
        serializer = TruyenSerializer(truyen_list, many=True, context={'request': request})
        return Response(serializer.data)


# =============================================
# THỂ LOẠI
# =============================================

class TheLoaiListView(APIView):
    """
    GET /api/genres
    Danh sách tất cả thể loại (public).
    """
    permission_classes = [AllowAny]

    def get(self, request):
        the_loai_list = TheLoai.objects.all()
        serializer = TheLoaiSerializer(the_loai_list, many=True)
        return Response(serializer.data)


class TruyenTheLoaiListView(APIView):
    """
    GET /api/stories/{story_id}/genres
    Lấy danh sách thể loại của một truyện.
    """
    permission_classes = [AllowAny]

    def get(self, request, story_id):
        try:
            truyen = Truyen.objects.get(pk=story_id)
        except Truyen.DoesNotExist:
            return Response({"error": "Không tìm thấy truyện."}, status=status.HTTP_404_NOT_FOUND)

        the_loai_list = truyen.the_loai.all()
        serializer = TheLoaiSerializer(the_loai_list, many=True)
        return Response(serializer.data)


# =============================================
# TÌM KIẾM
# =============================================

class SearchView(APIView):
    """
    GET /api/search?keyword=abc
    Tìm kiếm truyện và người dùng theo từ khóa.
    Trả kết quả chia 2 nhóm: stories và users.
    """
    permission_classes = [AllowAny]

    def get(self, request):
        keyword = request.query_params.get('keyword', '').strip()
        if not keyword:
            return Response({"error": "Thiếu từ khóa tìm kiếm."}, status=400)

        # Tìm truyện theo tên (chỉ truyện đã đăng)
        truyen_list = Truyen.objects.filter(
            ten_truyen__icontains=keyword,
            trang_thai='da_dang'
        )

        # Tìm user theo username
        user_list = NguoiDung.objects.filter(
            user__username__icontains=keyword
        )

        return Response({
            "keyword": keyword,
            "stories": TruyenSerializer(truyen_list, many=True, context={'request': request}).data,
            "users": [
                {
                    "id": u.id,
                    "username": u.user.username,
                    "avatar": request.build_absolute_uri(u.avatar.url) if u.avatar else None,
                    "so_truyen": u.truyen_list.filter(trang_thai='da_dang').count()
                }
                for u in user_list
            ]
        })


# =============================================
# LỊCH SỬ ĐỌC
# =============================================

class LichSuDocView(APIView):
    permission_classes = [IsAuthenticated]
    def get(self, request):
        profile, _ = get_nguoidung_or_error(request)
        ls = LichSuDoc.objects.filter(nguoi_dung=profile).order_by('-updated_at')
        return Response(LichSuDocSerializer(ls, many=True, context={'request': request}).data)

class UpdateLichSuDocView(APIView):
    permission_classes = [IsAuthenticated]
    def post(self, request):
        profile, err = get_nguoidung_or_error(request)
        if err:
            return err

        story_id   = request.data.get('story_id')
        chapter_id = request.data.get('chapter_id')

        if not story_id:
            return Response({"error": "Thiếu story_id."}, status=400)

        # Kiểm tra truyện tồn tại
        try:
            truyen = Truyen.objects.get(pk=story_id)
        except Truyen.DoesNotExist:
            return Response({"error": "Không tìm thấy truyện."}, status=404)

        # Kiểm tra chương (nếu có)
        chuong = None
        if chapter_id:
            try:
                chuong = Chuong.objects.get(pk=chapter_id, truyen=truyen)
            except Chuong.DoesNotExist:
                return Response({"error": "Không tìm thấy chương."}, status=404)

        # Dùng update_or_create: tạo mới hoặc cập nhật
        lich_su, created = LichSuDoc.objects.update_or_create(
            nguoi_dung=profile,
            truyen=truyen,
            defaults={'chuong': chuong}    # auto_now=True sẽ tự cập nhật updated_at
        )

        serializer = LichSuDocSerializer(lich_su, context={'request': request})
        return Response({
            "message": "Lưu lịch sử đọc thành công.",
            "created": created,
            "history": serializer.data
        }, status=201 if created else 200)


# =============================================
# QUÊN MẬT KHẨU
# =============================================

class QuenMatKhauView(APIView):
    permission_classes = [AllowAny]
    def post(self, request):
        # Lấy email từ request và chuẩn hóa
        email = request.data.get('email', '').strip().lower()
        if not email:
            return Response({"error": "Vui lòng cung cấp email."}, status=400)
            
        try:
            # Tìm user theo email (không phân biệt hoa thường)
            user = User.objects.get(email__iexact=email)
            otp = str(random.randint(100000, 999999))
            # Hết hạn sau 1 PHÚT (60 giây)
            cache.set(f"otp_{user.email}", otp, timeout=60)
            send_mail(
                "Mã xác nhận quên mật khẩu", 
                f"Mã của bạn: {otp}. Hiệu lực trong 1 phút.", 
                None, 
                [user.email]
            )
            return Response({"message": "OTP sent", "email": user.email})
        except User.DoesNotExist:
            return Response({"error": "Email không tồn tại trong hệ thống."}, status=404)

class XacNhanOTPView(APIView):
    permission_classes = [AllowAny]
    def post(self, request):
        email = request.data.get('email', '').strip().lower()
        otp = request.data.get('otp')
        otp_stored = cache.get(f"otp_{email}")
        
        if otp_stored is None:
            return Response({"error": "Mã OTP đã hết hạn."}, status=400)
            
        if str(otp_stored) == str(otp):
            token = str(random.randint(10000000, 99999999))
            cache.set(f"reset_{email}", token, timeout=600)
            cache.delete(f"otp_{email}")
            return Response({"reset_token": token})
        return Response({"error": "Mã OTP không chính xác."}, status=400)

class DatLaiMatKhauView(APIView):
    permission_classes = [AllowAny]
    def post(self, request):
        email = request.data.get('email', '').strip().lower()
        token = request.data.get('reset_token')
        pwd = request.data.get('new_password')
        if cache.get(f"reset_{email}") == str(token):
            try:
                u = User.objects.get(email__iexact=email)
                u.set_password(pwd)
                u.save()
                cache.delete(f"reset_{email}")
                return Response({"message": "Password updated"})
            except User.DoesNotExist:
                return Response({"error": "User không tồn tại."}, status=404)
        return Response({"error": "Yêu cầu không hợp lệ hoặc đã hết hạn."}, status=400)


# =============================================
# SOCIAL LOGIN
# =============================================

class SocialLoginView(APIView):
    permission_classes = [AllowAny]

    def post(self, request):
        provider = request.data.get('provider')  # 'google' hoặc 'facebook'
        access_token = request.data.get('access_token')

        if not provider or not access_token:
            return Response({"error": "Thiếu thông tin."}, status=400)

        email = None
        username = None

        # --- XỬ LÝ GOOGLE ---
        if provider == 'google':
            try:
                # Kiểm tra ID Token với Google
                # CLIENT_ID lấy từ Google Cloud Console
                id_info = id_token.verify_oauth2_token(access_token, google_requests.Request())
                email = id_info.get('email')
                username = email.split('@')[0]
            except Exception as e:
                return Response({"error": "Google Token không hợp lệ."}, status=400)

        # --- XỬ LÝ FACEBOOK ---
        elif provider == 'facebook':
            try:
                # Gọi API Graph của Facebook để lấy thông tin
                fb_url = f"https://graph.facebook.com/me?fields=id,name,email&access_token={access_token}"
                fb_resp = requests.get(fb_resp).json()
                if 'email' in fb_resp:
                    email = fb_resp['email']
                    username = fb_resp['name'].replace(" ", "_").lower()
                else:
                    return Response({"error": "Facebook không cung cấp email."}, status=400)
            except:
                return Response({"error": "Facebook Token không hợp lệ."}, status=400)

        # --- LIÊN KẾT VÀO HỆ THỐNG USER ---
        if email:
            # 1. Tìm hoặc tạo User dựa trên Email
            user, created = User.objects.get_or_create(email=email, defaults={
                'username': username,
            })

            # 2. Đảm bảo có profile NguoiDung
            profile, _ = NguoiDung.objects.get_or_create(user=user)

            # 3. Tạo/Lấy DRF Token để trả về cho Android
            drf_token, _ = Token.objects.get_or_create(user=user)

            return Response({
                "token": drf_token.key,
                "user_id": profile.id,
                "username": user.username,
                "is_new_user": created
            })

        return Response({"error": "Không thể xác thực."}, status=400)


# =============================================
# THEO DÕI NGƯỜI DÙNG
# =============================================

class DanhSachFollowerView(APIView):
    """Danh sách người đang theo dõi mình"""
    permission_classes = [IsAuthenticated]

    def get(self, request):
        profile, err = get_nguoidung_or_error(request)
        if err: return err
        followers = TheoDoiNguoiDung.objects.filter(
            nguoi_duoc_theo_doi=profile
        ).select_related('nguoi_theo_doi__user')
        data = [
            {
                "id": f.nguoi_theo_doi.id,
                "username": f.nguoi_theo_doi.user.username,
                "avatar": request.build_absolute_uri(f.nguoi_theo_doi.avatar.url) if f.nguoi_theo_doi.avatar else None
            }
            for f in followers
        ]
        return Response(data)


class DanhSachFollowingView(APIView):
    """Danh sách mình đang theo dõi"""
    permission_classes = [IsAuthenticated]

    def get(self, request):
        profile, err = get_nguoidung_or_error(request)
        if err: return err
        following = TheoDoiNguoiDung.objects.filter(
            nguoi_theo_doi=profile
        ).select_related('nguoi_duoc_theo_doi__user')
        data = [
            {
                "id": f.nguoi_duoc_theo_doi.id,
                "username": f.nguoi_duoc_theo_doi.user.username,
                "avatar": request.build_absolute_uri(f.nguoi_duoc_theo_doi.avatar.url) if f.nguoi_duoc_theo_doi.avatar else None
            }
            for f in following
        ]
        return Response(data)
