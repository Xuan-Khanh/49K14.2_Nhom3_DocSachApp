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
    NguoiDungSerializer, NguoiDungCapNhatSerializer,
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
# CÁC VIEW CƠ BẢN
# =============================================

class TruyenListCreateView(APIView):
    permission_classes = [IsAuthenticatedOrReadOnly]
    parser_classes = [MultiPartParser, FormParser, JSONParser]
    def get(self, request):
        queryset = Truyen.objects.filter(trang_thai__in=['da_dang', 'hoan_thanh'])
        theloai = request.query_params.get('theloai')
        if theloai: queryset = queryset.filter(the_loai__id=theloai)
        search = request.query_params.get('search')
        if search: queryset = queryset.filter(ten_truyen__icontains=search)
        serializer = TruyenSerializer(queryset.distinct(), many=True, context={'request': request})
        return Response(serializer.data)

    def post(self, request):
        profile, err = get_nguoidung_or_error(request)
        if err: return err
        serializer = TruyenSerializer(data=request.data, context={'request': request})
        if serializer.is_valid():
            truyen = serializer.save(nguoi_dung=profile)
            return Response(TruyenSerializer(truyen, context={'request': request}).data, status=201)
        return Response(serializer.errors, status=400)

class TruyenDetailView(APIView):
    permission_classes = [IsAuthenticatedOrReadOnly]
    def get(self, request, pk):
        try:
            truyen = Truyen.objects.get(pk=pk)
            truyen.so_luot_doc += 1
            truyen.save(update_fields=['so_luot_doc'])
            return Response(TruyenSerializer(truyen, context={'request': request}).data)
        except: return Response(status=404)

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

class ChuongListView(APIView):
    permission_classes = [AllowAny]
    def get(self, request, story_id):
        chuongs = Chuong.objects.filter(truyen_id=story_id, trang_thai='da_dang')
        return Response(ChuongNgoanSerializer(chuongs, many=True).data)

class ChuongDetailView(APIView):
    permission_classes = [AllowAny]
    def get(self, request, pk):
        try:
            chuong = Chuong.objects.get(pk=pk)
            return Response(ChuongSerializer(chuong).data)
        except: return Response(status=404)

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

class TheoDoiTruyenCreateView(APIView):
    permission_classes = [IsAuthenticated]
    def post(self, request):
        profile, _ = get_nguoidung_or_error(request)
        t = Truyen.objects.get(pk=request.data.get('story_id'))
        TheoDoiTruyen.objects.get_or_create(nguoi_dung=profile, truyen=t)
        return Response({"status": "followed"})

class BoSuuTapListCreateView(APIView):
    permission_classes = [IsAuthenticated]
    def get(self, request):
        profile, _ = get_nguoidung_or_error(request)
        bsts = BoSuuTap.objects.filter(nguoi_dung=profile)
        return Response(BoSuuTapSerializer(bsts, many=True, context={'request': request}).data)

class LichSuDocView(APIView):
    permission_classes = [IsAuthenticated]
    def get(self, request):
        profile, _ = get_nguoidung_or_error(request)
        ls = LichSuDoc.objects.filter(nguoi_dung=profile).order_by('-updated_at')
        return Response(LichSuDocSerializer(ls, many=True, context={'request': request}).data)

class UpdateLichSuDocView(APIView):
    permission_classes = [IsAuthenticated]
    def post(self, request):
        profile, _ = get_nguoidung_or_error(request)
        t = Truyen.objects.get(pk=request.data.get('story_id'))
        c = Chuong.objects.get(pk=request.data.get('chapter_id'))
        LichSuDoc.objects.update_or_create(nguoi_dung=profile, truyen=t, defaults={'chuong': c})
        return Response({"status": "updated"})

# QUÊN MẬT KHẨU
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

# Đăng nhập, Đăng ký qua google và facebook
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