"""
models.py – DocSachApp
======================
Ánh xạ 11 bảng nghiệp vụ thành Django models.
NguoiDung dùng OneToOneField với Django User để tận dụng:
  - auth (username, password hash, email)
  - Token authentication của DRF
"""

from django.db import models
from django.contrib.auth.models import User


# ==================== NGƯỜI DÙNG ====================

class NguoiDung(models.Model):
    """
    Profile mở rộng của User Django.
    User (built-in) giữ: username, email, password (đã hash)
    NguoiDung giữ: avatar, ngày sinh, mô tả
    """
    user = models.OneToOneField(
        User,
        on_delete=models.CASCADE,
        related_name='nguoidung',
        verbose_name='Tài khoản'
    )
    avatar = models.ImageField(
        upload_to='avatars/',
        null=True,
        blank=True,
        verbose_name='Ảnh đại diện'
    )
    ngay_sinh = models.DateField(
        null=True,
        blank=True,
        verbose_name='Ngày sinh'
    )
    mo_ta = models.TextField(
        null=True,
        blank=True,
        verbose_name='Mô tả'
    )

    class Meta:
        verbose_name = 'Người dùng'
        verbose_name_plural = 'Người dùng'

    def __str__(self):
        return self.user.username

    @property
    def ten_dang_nhap(self):
        return self.user.username

    @property
    def email(self):
        return self.user.email


# ==================== THỂ LOẠI ====================

class TheLoai(models.Model):
    """Bảng thể loại truyện (Action, Romance, Fantasy, ...)"""
    ten_the_loai = models.CharField(
        max_length=100,
        unique=True,
        verbose_name='Tên thể loại'
    )

    class Meta:
        verbose_name = 'Thể loại'
        verbose_name_plural = 'Thể loại'

    def __str__(self):
        return self.ten_the_loai


# ==================== TRUYỆN ====================

class Truyen(models.Model):
    """Bảng truyện – entity chính của hệ thống"""

    TRANG_THAI_CHOICES = [
        ('ban_thao', 'Bản thảo'),
        ('da_dang', 'Đã đăng'),
        ('hoan_thanh', 'Hoàn thành'),
    ]

    nguoi_dung = models.ForeignKey(
        NguoiDung,
        on_delete=models.CASCADE,
        related_name='truyen_list',
        verbose_name='Tác giả'
    )
    ten_truyen = models.CharField(max_length=255, verbose_name='Tên truyện')
    mo_ta = models.TextField(blank=True, verbose_name='Mô tả')
    trang_thai = models.CharField(
        max_length=20,
        choices=TRANG_THAI_CHOICES,
        default='ban_thao',
        verbose_name='Trạng thái'
    )
    anh_bia = models.ImageField(
        upload_to='covers/',
        null=True,
        blank=True,
        verbose_name='Ảnh bìa'
    )
    so_luot_doc = models.PositiveIntegerField(default=0, verbose_name='Số lượt đọc')

    # Quan hệ N-N với TheLoai qua TheLoaiTruyen
    the_loai = models.ManyToManyField(
        TheLoai,
        through='TheLoaiTruyen',
        related_name='truyen_list',
        blank=True,
        verbose_name='Thể loại'
    )

    class Meta:
        verbose_name = 'Truyện'
        verbose_name_plural = 'Truyện'
        ordering = ['-id']

    def __str__(self):
        return self.ten_truyen

    def diem_trung_binh(self):
        """Tính điểm trung bình rating"""
        danhgias = self.danh_gia_list.all()
        if not danhgias.exists():
            return 0
        return round(sum(d.sao_danh_gia for d in danhgias) / danhgias.count(), 1)

    def tong_danh_gia(self):
        return self.danh_gia_list.count()


# ==================== THỂ LOẠI - TRUYỆN (BẢNG TRUNG GIAN) ====================

class TheLoaiTruyen(models.Model):
    """Bảng trung gian N-N giữa Truyen và TheLoai"""
    truyen = models.ForeignKey(
        Truyen,
        on_delete=models.CASCADE,
        verbose_name='Truyện'
    )
    the_loai = models.ForeignKey(
        TheLoai,
        on_delete=models.CASCADE,
        verbose_name='Thể loại'
    )

    class Meta:
        verbose_name = 'Thể loại - Truyện'
        verbose_name_plural = 'Thể loại - Truyện'
        unique_together = ('truyen', 'the_loai')

    def __str__(self):
        return f"{self.truyen} - {self.the_loai}"


# ==================== CHƯƠNG ====================

class Chuong(models.Model):
    """Bảng chương của truyện"""

    TRANG_THAI_CHOICES = [
        ('ban_thao', 'Bản thảo'),
        ('da_dang', 'Đã đăng tải'),
    ]

    truyen = models.ForeignKey(
        Truyen,
        on_delete=models.CASCADE,
        related_name='chuong_list',
        verbose_name='Truyện'
    )
    tieu_de = models.CharField(max_length=255, verbose_name='Tiêu đề')
    noi_dung = models.TextField(blank=True, verbose_name='Nội dung')
    thoi_gian_tao = models.DateTimeField(auto_now_add=True, verbose_name='Thời gian tạo')
    trang_thai = models.CharField(
        max_length=20,
        choices=TRANG_THAI_CHOICES,
        default='ban_thao',
        verbose_name='Trạng thái'
    )
    thoi_gian_dang = models.DateTimeField(
        null=True,
        blank=True,
        verbose_name='Thời gian đăng'
    )

    class Meta:
        verbose_name = 'Chương'
        verbose_name_plural = 'Chương'
        ordering = ['id']

    def __str__(self):
        return f"{self.truyen.ten_truyen} - {self.tieu_de}"


# ==================== ĐÁNH GIÁ ====================

class DanhGia(models.Model):
    """Bảng đánh giá sao (1-5) của người dùng cho truyện"""
    nguoi_dung = models.ForeignKey(
        NguoiDung,
        on_delete=models.CASCADE,
        related_name='danh_gia_list',
        verbose_name='Người dùng'
    )
    truyen = models.ForeignKey(
        Truyen,
        on_delete=models.CASCADE,
        related_name='danh_gia_list',
        verbose_name='Truyện'
    )
    sao_danh_gia = models.PositiveSmallIntegerField(
        choices=[(i, i) for i in range(1, 6)],
        verbose_name='Số sao'
    )

    class Meta:
        verbose_name = 'Đánh giá'
        verbose_name_plural = 'Đánh giá'
        # Mỗi user chỉ đánh giá 1 lần cho mỗi truyện
        unique_together = ('nguoi_dung', 'truyen')

    def __str__(self):
        return f"{self.nguoi_dung} - {self.truyen} - {self.sao_danh_gia}★"


# ==================== BÌNH LUẬN ====================

class BinhLuan(models.Model):
    """Bảng bình luận của người dùng về truyện"""
    nguoi_dung = models.ForeignKey(
        NguoiDung,
        on_delete=models.CASCADE,
        related_name='binh_luan_list',
        verbose_name='Người dùng'
    )
    truyen = models.ForeignKey(
        Truyen,
        on_delete=models.CASCADE,
        related_name='binh_luan_list',
        verbose_name='Truyện'
    )
    noi_dung = models.TextField(verbose_name='Nội dung')
    thoi_gian_bl = models.DateTimeField(auto_now_add=True, verbose_name='Thời gian bình luận')

    class Meta:
        verbose_name = 'Bình luận'
        verbose_name_plural = 'Bình luận'
        ordering = ['-thoi_gian_bl']

    def __str__(self):
        return f"{self.nguoi_dung} bình luận {self.truyen}"


# ==================== THEO DÕI TRUYỆN ====================

class TheoDoiTruyen(models.Model):
    """Bảng người dùng theo dõi truyện"""
    nguoi_dung = models.ForeignKey(
        NguoiDung,
        on_delete=models.CASCADE,
        related_name='theo_doi_truyen_list',
        verbose_name='Người dùng'
    )
    truyen = models.ForeignKey(
        Truyen,
        on_delete=models.CASCADE,
        related_name='theo_doi_list',
        verbose_name='Truyện'
    )

    class Meta:
        verbose_name = 'Theo dõi truyện'
        verbose_name_plural = 'Theo dõi truyện'
        unique_together = ('nguoi_dung', 'truyen')

    def __str__(self):
        return f"{self.nguoi_dung} theo dõi {self.truyen}"


# ==================== THEO DÕI NGƯỜI DÙNG ====================

class TheoDoiNguoiDung(models.Model):
    """Bảng người dùng theo dõi người dùng khác"""
    nguoi_theo_doi = models.ForeignKey(
        NguoiDung,
        on_delete=models.CASCADE,
        related_name='dang_theo_doi',       # Danh sách mình đang follow ai
        verbose_name='Người theo dõi'
    )
    nguoi_duoc_theo_doi = models.ForeignKey(
        NguoiDung,
        on_delete=models.CASCADE,
        related_name='nguoi_theo_doi_list', # Danh sách follower của mình
        verbose_name='Người được theo dõi'
    )

    class Meta:
        verbose_name = 'Theo dõi người dùng'
        verbose_name_plural = 'Theo dõi người dùng'
        unique_together = ('nguoi_theo_doi', 'nguoi_duoc_theo_doi')

    def __str__(self):
        return f"{self.nguoi_theo_doi} → {self.nguoi_duoc_theo_doi}"


# ==================== BỘ SƯU TẬP ====================

class BoSuuTap(models.Model):
    """Bảng bộ sưu tập truyện của người dùng"""
    nguoi_dung = models.ForeignKey(
        NguoiDung,
        on_delete=models.CASCADE,
        related_name='bo_suu_tap_list',
        verbose_name='Chủ sở hữu'
    )
    ten_bo_suu_tap = models.CharField(max_length=200, verbose_name='Tên bộ sưu tập')

    # Quan hệ N-N với Truyen qua BoSuuTapTruyen
    truyen_list = models.ManyToManyField(
        Truyen,
        through='BoSuuTapTruyen',
        related_name='bo_suu_tap_list',
        blank=True,
        verbose_name='Danh sách truyện'
    )

    class Meta:
        verbose_name = 'Bộ sưu tập'
        verbose_name_plural = 'Bộ sưu tập'

    def __str__(self):
        return f"{self.nguoi_dung} - {self.ten_bo_suu_tap}"


# ==================== BỘ SƯU TẬP - TRUYỆN (BẢNG TRUNG GIAN) ====================

class BoSuuTapTruyen(models.Model):
    """Bảng trung gian N-N giữa BoSuuTap và Truyen"""
    bo_suu_tap = models.ForeignKey(
        BoSuuTap,
        on_delete=models.CASCADE,
        verbose_name='Bộ sưu tập'
    )
    truyen = models.ForeignKey(
        Truyen,
        on_delete=models.CASCADE,
        verbose_name='Truyện'
    )

    class Meta:
        verbose_name = 'Bộ sưu tập - Truyện'
        verbose_name_plural = 'Bộ sưu tập - Truyện'
        unique_together = ('bo_suu_tap', 'truyen')

    def __str__(self):
        return f"{self.bo_suu_tap} - {self.truyen}"
