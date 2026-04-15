"""
views.py – DocSachApp
======================
Tất cả API views dùng APIView (class-based) của Django REST Framework.
Tổ chức theo nhóm chức năng:
  1. Auth (đăng ký, đăng nhập, hồ sơ)
  2. User (xem profile, follow/unfollow người dùng)
  3. Truyện (CRUD + filter)
  4. Chương (CRUD + batch action)
  5. Bình luận
  6. Đánh giá sao
  7. Theo dõi truyện
  8. Bộ sưu tập
  9. Tìm kiếm
"""

from django.contrib.auth import authenticate
from django.contrib.auth.models import User
from django.utils import timezone
from django.db.models import Q

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
    """Lấy NguoiDung profile từ request.user. Trả về (profile, None) hoặc (None, Response lỗi)."""
    try:
        return request.user.nguoidung, None
    except NguoiDung.DoesNotExist:
        return None, Response(
            {"error": "Không tìm thấy profile người dùng."},
            status=status.HTTP_404_NOT_FOUND
        )


# =============================================
# 1. AUTH
# =============================================

class DangKyView(APIView):
    """
    POST /api/auth/register
    Đăng ký tài khoản mới. Không cần đăng nhập.
    """
    permission_classes = [AllowAny]
    parser_classes = [JSONParser, MultiPartParser, FormParser]

    def post(self, request):
        serializer = DangKySerializer(data=request.data)
        if serializer.is_valid():
            user = serializer.save()
            # Tạo token để trả về ngay
            token, _ = Token.objects.get_or_create(user=user)
            return Response({
                "message": "Đăng ký thành công.",
                "token": token.key,
                "user_id": user.nguoidung.id,
                "username": user.username
            }, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class DangNhapView(APIView):
    """
    POST /api/auth/login
    Đăng nhập, trả về token để dùng cho các request sau.
    Android lưu token này và gửi kèm trong header: Authorization: Token <token>
    """
    permission_classes = [AllowAny]

    def post(self, request):
        serializer = DangNhapSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

        username = serializer.validated_data['username']
        password = serializer.validated_data['password']
        user = authenticate(username=username, password=password)

        if user is None:
            return Response(
                {"error": "Tên đăng nhập hoặc mật khẩu không đúng."},
                status=status.HTTP_401_UNAUTHORIZED
            )

        token, _ = Token.objects.get_or_create(user=user)
        try:
            profile = user.nguoidung
        except NguoiDung.DoesNotExist:
            # Tạo profile nếu chưa có (edge case)
            profile = NguoiDung.objects.create(user=user)

        return Response({
            "message": "Đăng nhập thành công.",
            "token": token.key,
            "user_id": profile.id,
            "username": user.username,
            "avatar": request.build_absolute_uri(profile.avatar.url) if profile.avatar else None
        }, status=status.HTTP_200_OK)


class HoSoView(APIView):
    """
    GET  /api/auth/profile  – Lấy hồ sơ người dùng đang đăng nhập
    PUT  /api/auth/profile  – Cập nhật hồ sơ (chỉ user đó mới được sửa)
    """
    permission_classes = [IsAuthenticated]
    parser_classes = [MultiPartParser, FormParser, JSONParser]

    def get(self, request):
        profile, err = get_nguoidung_or_error(request)
        if err:
            return err
        serializer = NguoiDungSerializer(profile, context={'request': request})
        return Response(serializer.data)

    def put(self, request):
        profile, err = get_nguoidung_or_error(request)
        if err:
            return err
        serializer = NguoiDungCapNhatSerializer(
            profile, data=request.data,
            partial=True, context={'request': request}
        )
        if serializer.is_valid():
            serializer.save()
            return Response({
                "message": "Cập nhật thành công.",
                "profile": NguoiDungSerializer(profile, context={'request': request}).data
            })
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


# =============================================
# 2. USER
# =============================================

class UserDetailView(APIView):
    """
    GET /api/users/{id}
    Xem thông tin công khai của người dùng khác.
    Trả thêm: số truyện, follower, following, danh sách truyện đã đăng.
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
# 3. TRUYỆN
# =============================================

class TruyenListCreateView(APIView):
    """
    GET  /api/stories          – Danh sách truyện (public), có filter và search
    POST /api/stories          – Tạo truyện mới (cần đăng nhập)

    Query params:
      - theloai=<id>   : lọc theo thể loại
      - search=<text>  : tìm kiếm theo tên truyện
      - trang_thai=<x> : lọc theo trạng thái (mặc định chỉ lấy da_dang)
    """
    permission_classes = [IsAuthenticatedOrReadOnly]
    parser_classes = [MultiPartParser, FormParser, JSONParser]

    def get(self, request):
        # Mặc định chỉ hiển thị truyện đã đăng cho public
        queryset = Truyen.objects.filter(trang_thai='da_dang')

        # Filter theo thể loại
        theloai_id = request.query_params.get('theloai')
        if theloai_id:
            queryset = queryset.filter(the_loai__id=theloai_id)

        # Tìm kiếm theo tên truyện
        search = request.query_params.get('search')
        if search:
            queryset = queryset.filter(ten_truyen__icontains=search)

        serializer = TruyenSerializer(queryset.distinct(), many=True, context={'request': request})
        return Response(serializer.data)

    def post(self, request):
        if not request.user.is_authenticated:
            return Response({"error": "Cần đăng nhập."}, status=401)

        profile, err = get_nguoidung_or_error(request)
        if err:
            return err

        serializer = TruyenSerializer(data=request.data, context={'request': request})
        if serializer.is_valid():
            truyen = serializer.save(nguoi_dung=profile)
            return Response(
                TruyenSerializer(truyen, context={'request': request}).data,
                status=201
            )
        return Response(serializer.errors, status=400)


class TruyenDetailView(APIView):
    """
    GET    /api/stories/{id}  – Chi tiết truyện (public)
    PUT    /api/stories/{id}  – Sửa truyện (chỉ tác giả)
    DELETE /api/stories/{id}  – Xóa truyện (chỉ tác giả)
    """
    permission_classes = [IsAuthenticatedOrReadOnly]
    parser_classes = [MultiPartParser, FormParser, JSONParser]

    def get_object(self, pk):
        try:
            return Truyen.objects.get(pk=pk)
        except Truyen.DoesNotExist:
            return None

    def get(self, request, pk):
        truyen = self.get_object(pk)
        if not truyen:
            return Response({"error": "Không tìm thấy truyện."}, status=404)
        # Tăng lượt đọc
        truyen.so_luot_doc += 1
        truyen.save(update_fields=['so_luot_doc'])
        serializer = TruyenSerializer(truyen, context={'request': request})
        return Response(serializer.data)

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

        serializer = TruyenSerializer(
            truyen, data=request.data, partial=True, context={'request': request}
        )
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
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

        # CASCADE xóa tất cả dữ liệu liên quan (chapters, comments, ratings, follows)
        truyen.delete()
        return Response({"message": "Đã xóa truyện và dữ liệu liên quan."})


# =============================================
# 4. CHƯƠNG
# =============================================

class ChuongListView(APIView):
    """
    GET /api/stories/{id}/chapters
    Lấy danh sách chương của một truyện (public).
    """
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
    """
    GET /api/chapters/{id}
    Chi tiết một chương (public nếu đã đăng, tác giả thấy bản thảo).
    """
    permission_classes = [AllowAny]

    def get(self, request, pk):
        try:
            chuong = Chuong.objects.get(pk=pk)
        except Chuong.DoesNotExist:
            return Response({"error": "Không tìm thấy chương."}, status=404)

        # Kiểm tra quyền xem bản thảo
        if chuong.trang_thai == 'ban_thao':
            if not request.user.is_authenticated:
                return Response({"error": "Chương chưa được đăng tải."}, status=403)
            try:
                if chuong.truyen.nguoi_dung != request.user.nguoidung:
                    return Response({"error": "Chương chưa được đăng tải."}, status=403)
            except Exception:
                return Response({"error": "Chương chưa được đăng tải."}, status=403)

        serializer = ChuongSerializer(chuong)
        return Response(serializer.data)


class ChuongCreateView(APIView):
    """
    POST /api/chapters
    Tạo chương mới. Chỉ tác giả của truyện mới được tạo.
    Body: {truyen_id, tieu_de, noi_dung, trang_thai}
    """
    permission_classes = [IsAuthenticated]

    def post(self, request):
        profile, err = get_nguoidung_or_error(request)
        if err:
            return err

        serializer = ChuongSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=400)

        truyen_id = serializer.validated_data.get('truyen_id')
        try:
            truyen = Truyen.objects.get(pk=truyen_id)
        except Truyen.DoesNotExist:
            return Response({"error": "Không tìm thấy truyện."}, status=404)

        # Kiểm tra quyền tác giả
        if truyen.nguoi_dung != profile:
            return Response({"error": "Bạn không phải tác giả của truyện này."}, status=403)

        chuong = Chuong(
            truyen=truyen,
            tieu_de=serializer.validated_data['tieu_de'],
            noi_dung=serializer.validated_data.get('noi_dung', ''),
            trang_thai=serializer.validated_data.get('trang_thai', 'ban_thao')
        )
        # Nếu đăng ngay thì lưu thời gian đăng
        if chuong.trang_thai == 'da_dang':
            chuong.thoi_gian_dang = timezone.now()
        chuong.save()

        return Response(ChuongSerializer(chuong).data, status=201)


class ChuongUpdateDeleteView(APIView):
    """
    PUT    /api/chapters/{id}  – Sửa chương
    DELETE /api/chapters/{id}  – Xóa chương
    Chỉ tác giả của truyện mới được thao tác.
    """
    permission_classes = [IsAuthenticated]

    def get_object(self, pk, profile):
        try:
            chuong = Chuong.objects.get(pk=pk)
        except Chuong.DoesNotExist:
            return None, Response({"error": "Không tìm thấy chương."}, status=404)
        if chuong.truyen.nguoi_dung != profile:
            return None, Response({"error": "Bạn không phải tác giả của truyện này."}, status=403)
        return chuong, None

    def put(self, request, pk):
        profile, err = get_nguoidung_or_error(request)
        if err:
            return err
        chuong, err = self.get_object(pk, profile)
        if err:
            return err

        serializer = ChuongSerializer(chuong, data=request.data, partial=True)
        if not serializer.is_valid():
            return Response(serializer.errors, status=400)

        # Xử lý thời gian đăng khi đổi sang trạng thái 'da_dang'
        trang_thai_moi = serializer.validated_data.get('trang_thai', chuong.trang_thai)
        if trang_thai_moi == 'da_dang' and chuong.trang_thai == 'ban_thao':
            serializer.save(thoi_gian_dang=timezone.now())
        else:
            serializer.save()

        return Response(ChuongSerializer(chuong).data)

    def delete(self, request, pk):
        profile, err = get_nguoidung_or_error(request)
        if err:
            return err
        chuong, err = self.get_object(pk, profile)
        if err:
            return err
        chuong.delete()
        return Response({"message": "Đã xóa chương."})


class ChuongBatchActionView(APIView):
    """
    POST /api/chapters/batch-action
    Thao tác nhiều chương cùng lúc.
    Body: {
      "chapter_ids": [1, 2, 3],
      "action": "publish" | "unpublish" | "delete"
    }
    Logic:
      - Tất cả bản thảo → cho publish hoặc delete
      - Tất cả đã đăng  → cho unpublish hoặc delete
      - Lẫn lộn         → chỉ cho delete
    """
    permission_classes = [IsAuthenticated]

    def post(self, request):
        profile, err = get_nguoidung_or_error(request)
        if err:
            return err

        chapter_ids = request.data.get('chapter_ids', [])
        action = request.data.get('action', '')

        if not chapter_ids or action not in ['publish', 'unpublish', 'delete']:
            return Response({"error": "Thiếu chapter_ids hoặc action không hợp lệ."}, status=400)

        # Lấy danh sách chương thuộc quyền tác giả
        chapters = Chuong.objects.filter(id__in=chapter_ids, truyen__nguoi_dung=profile)
        if chapters.count() != len(chapter_ids):
            return Response({"error": "Một số chương không hợp lệ hoặc không thuộc quyền bạn."}, status=403)

        trang_thai_set = set(chapters.values_list('trang_thai', flat=True))
        all_draft = trang_thai_set == {'ban_thao'}
        all_published = trang_thai_set == {'da_dang'}

        if action == 'publish':
            if not all_draft:
                return Response({"error": "Chỉ có thể đăng khi tất cả chương là bản thảo."}, status=400)
            # Validate tiêu đề/nội dung trước khi đăng
            invalid = [c.tieu_de for c in chapters if not c.tieu_de.strip() or not c.noi_dung.strip()]
            if invalid:
                return Response({"error": f"Chương thiếu tiêu đề/nội dung: {invalid}"}, status=400)
            chapters.update(trang_thai='da_dang', thoi_gian_dang=timezone.now())
            return Response({"message": f"Đã đăng {len(chapter_ids)} chương."})

        elif action == 'unpublish':
            if not all_published:
                return Response({"error": "Chỉ có thể ngừng đăng khi tất cả chương đã đăng."}, status=400)
            chapters.update(trang_thai='ban_thao')
            return Response({"message": f"Đã ngừng đăng {len(chapter_ids)} chương."})

        elif action == 'delete':
            count = chapters.count()
            chapters.delete()
            return Response({"message": f"Đã xóa {count} chương."})


# =============================================
# 5. BÌNH LUẬN
# =============================================

class BinhLuanListView(APIView):
    """
    GET /api/stories/{id}/comments
    Danh sách bình luận của một truyện (public), kèm thông tin người dùng.
    """
    permission_classes = [AllowAny]

    def get(self, request, story_id):
        binh_luan_list = BinhLuan.objects.filter(truyen_id=story_id).select_related(
            'nguoi_dung__user'
        )
        serializer = BinhLuanSerializer(binh_luan_list, many=True, context={'request': request})
        return Response(serializer.data)


class BinhLuanCreateView(APIView):
    """
    POST /api/comments
    Tạo bình luận. User phải đăng nhập.
    Body: {truyen_id, noi_dung}
    """
    permission_classes = [IsAuthenticated]

    def post(self, request):
        profile, err = get_nguoidung_or_error(request)
        if err:
            return err

        serializer = BinhLuanSerializer(data=request.data, context={'request': request})
        if serializer.is_valid():
            truyen_id = serializer.validated_data['truyen_id']
            try:
                truyen = Truyen.objects.get(pk=truyen_id)
            except Truyen.DoesNotExist:
                return Response({"error": "Không tìm thấy truyện."}, status=404)

            binh_luan = BinhLuan.objects.create(
                nguoi_dung=profile,
                truyen=truyen,
                noi_dung=serializer.validated_data['noi_dung']
            )
            return Response(
                BinhLuanSerializer(binh_luan, context={'request': request}).data,
                status=201
            )
        return Response(serializer.errors, status=400)


class BinhLuanDeleteView(APIView):
    """
    DELETE /api/comments/{id}
    Xóa bình luận. Chỉ người tạo hoặc admin mới được xóa.
    """
    permission_classes = [IsAuthenticated]

    def delete(self, request, pk):
        try:
            binh_luan = BinhLuan.objects.get(pk=pk)
        except BinhLuan.DoesNotExist:
            return Response({"error": "Không tìm thấy bình luận."}, status=404)

        profile, err = get_nguoidung_or_error(request)
        if err:
            return err

        # Chỉ người tạo hoặc admin mới được xóa
        if binh_luan.nguoi_dung != profile and not request.user.is_staff:
            return Response({"error": "Bạn không có quyền xóa bình luận này."}, status=403)

        binh_luan.delete()
        return Response({"message": "Đã xóa bình luận."})


# =============================================
# 6. ĐÁNH GIÁ SAO
# =============================================

class DanhGiaCreateView(APIView):
    """
    POST /api/ratings
    Đánh giá truyện. Nếu đã đánh giá thì cập nhật (không tạo bản ghi mới).
    Body: {story_id, rating}
    """
    permission_classes = [IsAuthenticated]

    def post(self, request):
        profile, err = get_nguoidung_or_error(request)
        if err:
            return err

        story_id = request.data.get('story_id')
        rating = request.data.get('rating')

        # Validate
        if story_id is None or rating is None:
            return Response({"error": "Thiếu story_id hoặc rating."}, status=400)
        try:
            rating = int(rating)
        except (ValueError, TypeError):
            return Response({"error": "rating phải là số nguyên."}, status=400)
        if not (1 <= rating <= 5):
            return Response({"error": "rating phải từ 1 đến 5."}, status=400)

        try:
            truyen = Truyen.objects.get(pk=story_id)
        except Truyen.DoesNotExist:
            return Response({"error": "Không tìm thấy truyện."}, status=404)

        # Dùng update_or_create để không tạo trùng
        danh_gia, created = DanhGia.objects.update_or_create(
            nguoi_dung=profile,
            truyen=truyen,
            defaults={'sao_danh_gia': rating}
        )
        msg = "Đánh giá thành công." if created else "Cập nhật đánh giá thành công."
        return Response({
            "message": msg,
            "rating": rating,
            "diem_trung_binh": truyen.diem_trung_binh(),
            "tong_danh_gia": truyen.tong_danh_gia()
        }, status=201 if created else 200)


class DanhGiaListView(APIView):
    """
    GET /api/stories/{id}/ratings
    Danh sách đánh giá + điểm trung bình của một truyện.
    """
    permission_classes = [AllowAny]

    def get(self, request, story_id):
        try:
            truyen = Truyen.objects.get(pk=story_id)
        except Truyen.DoesNotExist:
            return Response({"error": "Không tìm thấy truyện."}, status=404)

        danh_gia_list = truyen.danh_gia_list.select_related('nguoi_dung__user')
        serializer = DanhGiaSerializer(danh_gia_list, many=True)
        return Response({
            "diem_trung_binh": truyen.diem_trung_binh(),
            "tong_danh_gia": truyen.tong_danh_gia(),
            "danh_gia": serializer.data
        })


# =============================================
# 7. THEO DÕI TRUYỆN
# =============================================

class TheoDoiTruyenCreateView(APIView):
    """
    POST /api/follow/story
    Body: {story_id}
    Theo dõi một truyện.
    """
    permission_classes = [IsAuthenticated]

    def post(self, request):
        profile, err = get_nguoidung_or_error(request)
        if err:
            return err

        story_id = request.data.get('story_id')
        if not story_id:
            return Response({"error": "Thiếu story_id."}, status=400)

        try:
            truyen = Truyen.objects.get(pk=story_id)
        except Truyen.DoesNotExist:
            return Response({"error": "Không tìm thấy truyện."}, status=404)

        if TheoDoiTruyen.objects.filter(nguoi_dung=profile, truyen=truyen).exists():
            return Response({"message": "Bạn đã theo dõi truyện này.", "is_following": True})

        TheoDoiTruyen.objects.create(nguoi_dung=profile, truyen=truyen)
        return Response({"message": "Đã theo dõi truyện.", "is_following": True}, status=201)


class BorTheoDoiTruyenView(APIView):
    """
    DELETE /api/unfollow/story
    Body: {story_id}
    Bỏ theo dõi truyện.
    """
    permission_classes = [IsAuthenticated]

    def delete(self, request):
        profile, err = get_nguoidung_or_error(request)
        if err:
            return err

        story_id = request.data.get('story_id')
        deleted, _ = TheoDoiTruyen.objects.filter(
            nguoi_dung=profile, truyen_id=story_id
        ).delete()
        if deleted:
            return Response({"message": "Đã bỏ theo dõi.", "is_following": False})
        return Response({"error": "Bạn chưa theo dõi truyện này."}, status=400)


class DanhSachTheoDoiTruyenView(APIView):
    """
    GET /api/user/following-stories
    Danh sách truyện đang theo dõi của user hiện tại.
    FIX: Trả về flat List[Truyen] thay vì TheoDoiTruyen wrapper
    để Android parse trực tiếp thành List<Story>.
    """
    permission_classes = [IsAuthenticated]

    def get(self, request):
        profile, err = get_nguoidung_or_error(request)
        if err:
            return err
        # Lấy danh sách truyện trực tiếp (không wrap trong TheoDoiTruyen)
        theo_doi_list = TheoDoiTruyen.objects.filter(
            nguoi_dung=profile
        ).select_related('truyen')
        truyen_list = [td.truyen for td in theo_doi_list]
        serializer = TruyenSerializer(truyen_list, many=True, context={'request': request})
        return Response(serializer.data)


# =============================================
# 8. BỘ SƯU TẬP
# =============================================

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
    """
    GET  /api/collections  – Danh sách bộ sưu tập của user đang đăng nhập
    POST /api/collections  – Tạo bộ sưu tập mới
    Body POST: {ten_bo_suu_tap}
    """
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
# 9. THỂ LOẠI
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


# =============================================
# 10. TÌM KIẾM
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
# 10. LỊCH SỬ ĐỌC
# =============================================

class LichSuDocView(APIView):
    """
    GET /api/reading-history
    Trả về lịch sử đọc của user hiện tại.
    - Chỉ user đăng nhập mới xem được
    - Sắp xếp theo đọc gần nhất (updated_at DESC)
    """
    permission_classes = [IsAuthenticated]

    def get(self, request):
        profile, err = get_nguoidung_or_error(request)
        if err:
            return err

        # Lấy lịch sử đọc, eager load để tránh N+1 query
        lich_su = LichSuDoc.objects.filter(
            nguoi_dung=profile
        ).select_related(
            'truyen__nguoi_dung__user',   # để lấy tên tác giả
            'chuong',                      # để lấy chương hiện tại
        ).prefetch_related(
            'truyen__the_loai',            # để lấy danh sách thể loại
            'truyen__theo_doi_list',       # để đếm followers
        ).order_by('-updated_at')          # Mới nhất trước

        serializer = LichSuDocSerializer(
            lich_su, many=True, context={'request': request}
        )
        return Response(serializer.data)


class UpdateLichSuDocView(APIView):
    """
    POST /api/reading-history/update
    Ghi hoặc cập nhật lịch sử đọc khi user mở một chương.
    Body: {
        "story_id": 5,
        "chapter_id": 12
    }
    Logic:
      - Nếu chưa có bản ghi → tạo mới
      - Nếu đã có → update chương và updated_at
    """
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