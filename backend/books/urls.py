"""
urls.py – books app (DocSachApp)
=================================
Tất cả 35+ endpoint API theo đúng spec.
Prefix /api/ được thêm từ root urls.py.
"""

from django.urls import path
from . import views

urlpatterns = [

    # ==================== AUTH ====================
    # POST /api/auth/register  – Đăng ký
    path('auth/register', views.DangKyView.as_view(), name='auth-register'),
    # POST /api/auth/login     – Đăng nhập, lấy token
    path('auth/login', views.DangNhapView.as_view(), name='auth-login'),
    # GET/PUT /api/auth/profile – Hồ sơ người dùng đang đăng nhập
    path('auth/profile', views.HoSoView.as_view(), name='auth-profile'),

    # ==================== USER ====================
    # GET    /api/users/{id}      – Xem profile công khai
    path('users/<int:pk>', views.UserDetailView.as_view(), name='user-detail'),
    # POST   /api/users/follow    – Theo dõi người dùng
    path('users/follow', views.FollowUserView.as_view(), name='user-follow'),
    # DELETE /api/users/unfollow  – Bỏ theo dõi người dùng
    path('users/unfollow', views.UnfollowUserView.as_view(), name='user-unfollow'),
<<<<<<< HEAD
    # GET /api/users/{user_id}/collections - Lấy danh sách bộ sưu tập của người dùng
    path('users/<int:user_id>/collections', views.UserBoSuuTapListView.as_view(), name='user-collection-list'),
=======
    # GET    /api/users/{id}/followers – Lấy danh sách người đang theo dõi user này
    path('users/<int:pk>/followers', views.FollowerListView.as_view(), name='user-followers'),
    # GET    /api/users/{id}/following – Lấy danh sách người mà user này đang theo dõi
    path('users/<int:pk>/following', views.FollowingListView.as_view(), name='user-following'),
>>>>>>> origin/phuong

    # ==================== TRUYỆN ====================
    # GET  /api/stories           – Danh sách truyện (public, có filter)
    # POST /api/stories           – Tạo truyện mới
    path('stories', views.TruyenListCreateView.as_view(), name='story-list-create'),
    # GET    /api/stories/{id}    – Chi tiết truyện
    # PUT    /api/stories/{id}    – Sửa truyện
    # DELETE /api/stories/{id}    – Xóa truyện
    path('stories/<int:pk>', views.TruyenDetailView.as_view(), name='story-detail'),
    # GET /api/stories/{story_id}/genres - Lấy danh sách thể loại của một truyện
    path('stories/<int:story_id>/genres', views.TruyenTheLoaiListView.as_view(), name='story-genre-list'),

    # ==================== CHƯƠNG ====================
    # GET /api/stories/{id}/chapters  – Danh sách chương của truyện
    path('stories/<int:story_id>/chapters', views.ChuongListView.as_view(), name='chapter-list'),
    # GET /api/chapters/{id}          – Chi tiết chương
    path('chapters/<int:pk>', views.ChuongDetailView.as_view(), name='chapter-detail'),
    # POST /api/chapters              – Tạo chương mới
    path('chapters', views.ChuongCreateView.as_view(), name='chapter-create'),
    # PUT/DELETE /api/chapters/{id}   – Sửa/xóa chương
    path('chapters/<int:pk>/edit', views.ChuongUpdateDeleteView.as_view(), name='chapter-edit'),
    # POST /api/chapters/batch-action – Thao tác nhiều chương
    path('chapters/batch-action', views.ChuongBatchActionView.as_view(), name='chapter-batch'),

    # ==================== BÌNH LUẬN ====================
    # GET  /api/stories/{id}/comments – Danh sách bình luận
    path('stories/<int:story_id>/comments', views.BinhLuanListView.as_view(), name='comment-list'),
    # POST /api/comments              – Tạo bình luận
    path('comments', views.BinhLuanCreateView.as_view(), name='comment-create'),
    # DELETE /api/comments/{id}       – Xóa bình luận
    path('comments/<int:pk>', views.BinhLuanDeleteView.as_view(), name='comment-delete'),

    # ==================== ĐÁNH GIÁ ====================
    # POST /api/ratings               – Đánh giá hoặc cập nhật
    path('ratings', views.DanhGiaCreateView.as_view(), name='rating-create'),
    # GET  /api/stories/{id}/ratings  – Danh sách đánh giá + điểm TB
    path('stories/<int:story_id>/ratings', views.DanhGiaListView.as_view(), name='rating-list'),

    # ==================== THEO DÕI TRUYỆN ====================
    # POST   /api/follow/story         – Theo dõi truyện
    path('follow/story', views.TheoDoiTruyenCreateView.as_view(), name='follow-story'),
    # DELETE /api/unfollow/story       – Bỏ theo dõi truyện
    path('unfollow/story', views.BorTheoDoiTruyenView.as_view(), name='unfollow-story'),
    # GET    /api/user/following-stories – Danh sách truyện đang theo dõi
    path('user/following-stories', views.DanhSachTheoDoiTruyenView.as_view(), name='following-stories'),

    # ==================== BỘ SƯU TẬP ====================
    # GET  /api/bosuutap       – Danh sách tất cả BST (public)
    path('bosuutap', views.BoSuuTapListView.as_view(), name='bosuutap-list'),
    # GET  /api/collections       – Danh sách BST của user
    # POST /api/collections       – Tạo BST mới
    path('collections', views.BoSuuTapListCreateView.as_view(), name='collection-list-create'),
    # GET    /api/collections/{id} – Chi tiết BST kèm truyện
    # PUT    /api/collections/{id} – Đổi tên BST
    # DELETE /api/collections/{id} – Xóa BST
    path('collections/<int:pk>', views.BoSuuTapDetailView.as_view(), name='collection-detail'),
    # POST   /api/collections/add-story    – Thêm truyện vào BST
    path('collections/add-story', views.ThemTruyenVaoBSTView.as_view(), name='collection-add-story'),
    # DELETE /api/collections/remove-story – Xóa truyện khỏi BST
    path('collections/remove-story', views.XoaTruyenKhoiBSTView.as_view(), name='collection-remove-story'),
    # GET /api/collections/{id}/stories – Lấy danh sách truyện trong một bộ sưu tập
    path('collections/<int:pk>/stories', views.TruyenInBoSuuTapView.as_view(), name='truyen-in-bosuutap'),


    # ==================== THỂ LOẠI ====================
    # GET /api/genres – Danh sách thể loại
    path('genres', views.TheLoaiListView.as_view(), name='genre-list'),

    # ==================== TÌM KIẾM ====================
    # GET /api/search?keyword=abc
    path('search', views.SearchView.as_view(), name='search'),

    # ==================== LỊCH SỬ ĐỌC ====================
    # GET  /api/reading-history        – Lịch sử đọc của user hiện tại
    path('reading-history', views.LichSuDocView.as_view(), name='reading-history'),
    # POST /api/reading-history/update – Cập nhật lịch sử khi mở chương
    path('reading-history/update', views.UpdateLichSuDocView.as_view(), name='reading-history-update'),
]
