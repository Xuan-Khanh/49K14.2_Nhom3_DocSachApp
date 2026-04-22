"""
urls.py – DocSachApp
"""

from django.urls import path
from . import views

urlpatterns = [

    # ==================== AUTH ====================
    path('auth/register/', views.DangKyView.as_view(), name='auth-register'),
    path('auth/login/', views.DangNhapView.as_view(), name='auth-login'),
    path('auth/profile/', views.HoSoView.as_view(), name='auth-profile'),
    path('auth/social-login/', views.SocialLoginView.as_view(), name='social-login'),
    # QUÊN MẬT KHẨU
    path('auth/forgot-password/', views.QuenMatKhauView.as_view(), name='forgot-password'),
    path('auth/verify-otp/', views.XacNhanOTPView.as_view(), name='verify-otp'),
    path('auth/reset-password/', views.DatLaiMatKhauView.as_view(), name='reset-password'),

    # ==================== TRUYỆN - PHÂN LOẠI DANH MỤC ====================
    # Các API này phục vụ cho Trang chủ Frontend
    path('stories/new-releases/', views.TruyenMoiDangView.as_view(), name='stories-new'),
    path('stories/recently-updated/', views.TruyenMoiCapNhatView.as_view(), name='stories-updated'),
    path('stories/completed/', views.TruyenHoanThanhView.as_view(), name='stories-completed'),

    # ==================== TRUYỆN CƠ BẢN ====================
    path('stories/', views.TruyenListCreateView.as_view(), name='story-list-create'),
    path('stories/<int:pk>/', views.TruyenDetailView.as_view(), name='story-detail'),

    # ==================== CHƯƠNG ====================
    path('stories/<int:story_id>/chapters/', views.ChuongListView.as_view(), name='chapter-list'),
    path('chapters/<int:pk>/', views.ChuongDetailView.as_view(), name='chapter-detail'),

    # ==================== BÌNH LUẬN & ĐÁNH GIÁ ====================
    path('stories/<int:story_id>/comments/', views.BinhLuanListView.as_view(), name='comment-list'),
    path('ratings/', views.DanhGiaCreateView.as_view(), name='rating-create'),

    # ==================== THEO DÕI & BỘ SƯU TẬP ====================
    path('follow/story/', views.TheoDoiTruyenCreateView.as_view(), name='follow-story'),
    path('collections/', views.BoSuuTapListCreateView.as_view(), name='collection-list-create'),

    # ==================== LỊCH SỬ ĐỌC ====================
    path('reading-history/', views.LichSuDocView.as_view(), name='reading-history'),
    path('reading-history/update/', views.UpdateLichSuDocView.as_view(), name='reading-history-update'),
# =============================================THEO DÕI NGƯỜI DÙNG=============================================
    path('auth/followers/', views.DanhSachFollowerView.as_view(), name='followers'),
    path('auth/following/', views.DanhSachFollowingView.as_view(), name='following'),
]
