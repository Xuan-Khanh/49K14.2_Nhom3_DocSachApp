"""
models.py – DocSachApp
"""

from django.db import models
from django.contrib.auth.models import User

# ==================== NGƯỜI DÙNG ====================
class NguoiDung(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE, related_name='nguoidung', verbose_name='Tài khoản')
    avatar = models.ImageField(upload_to='avatars/', null=True, blank=True, verbose_name='Ảnh đại diện')
    ngay_sinh = models.DateField(null=True, blank=True, verbose_name='Ngày sinh')
    mo_ta = models.TextField(null=True, blank=True, verbose_name='Mô tả')

    class Meta:
        verbose_name = 'Người dùng'
        verbose_name_plural = 'Người dùng'

    def __str__(self):
        return self.user.username

# ==================== THỂ LOẠI ====================
class TheLoai(models.Model):
    ten_the_loai = models.CharField(max_length=100, unique=True, verbose_name='Tên thể loại')

    class Meta:
        verbose_name = 'Thể loại'
        verbose_name_plural = 'Thể loại'

    def __str__(self):
        return self.ten_the_loai

# ==================== TRUYỆN ====================
class Truyen(models.Model):
    TRANG_THAI_CHOICES = [
        ('ban_thao', 'Bản thảo'),
        ('da_dang', 'Đã đăng'),
        ('hoan_thanh', 'Hoàn thành'),
    ]

    nguoi_dung = models.ForeignKey(NguoiDung, on_delete=models.CASCADE, related_name='truyen_list', verbose_name='Tác giả')
    ten_truyen = models.CharField(max_length=255, verbose_name='Tên truyện')
    mo_ta = models.TextField(blank=True, verbose_name='Mô tả')
    trang_thai = models.CharField(max_length=20, choices=TRANG_THAI_CHOICES, default='ban_thao', verbose_name='Trạng thái')
    anh_bia = models.ImageField(upload_to='covers/', null=True, blank=True, verbose_name='Ảnh bìa')
    so_luot_doc = models.PositiveIntegerField(default=0, verbose_name='Số lượt đọc')
    
    created_at = models.DateTimeField(auto_now_add=True, null=True, verbose_name='Ngày đăng')
    updated_at = models.DateTimeField(auto_now=True, null=True, verbose_name='Cập nhật mới nhất')

    the_loai = models.ManyToManyField(TheLoai, through='TheLoaiTruyen', related_name='truyen_list', blank=True, verbose_name='Thể loại')

    class Meta:
        verbose_name = 'Truyện'
        verbose_name_plural = 'Truyện'
        ordering = ['-updated_at']

    def __str__(self):
        return self.ten_truyen

    def diem_trung_binh(self):
        danhgias = self.danhgia_set.all()
        if not danhgias.exists(): return 0
        return round(sum(d.sao_danh_gia for d in danhgias) / danhgias.count(), 1)

    def tong_danh_gia(self):
        return self.danhgia_set.count()

# ==================== THỂ LOẠI - TRUYỆN (BẢNG TRUNG GIAN) ====================
class TheLoaiTruyen(models.Model):
    truyen = models.ForeignKey(Truyen, on_delete=models.CASCADE, verbose_name='Truyện')
    the_loai = models.ForeignKey(TheLoai, on_delete=models.CASCADE, verbose_name='Thể loại')
    class Meta:
        verbose_name = 'Thể loại - Truyện'
        verbose_name_plural = 'Thể loại - Truyện'
        unique_together = ('truyen', 'the_loai')

# ==================== CHƯƠNG ====================
class Chuong(models.Model):
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
    
    so_luot_doc = models.PositiveIntegerField(default=0, verbose_name='Số lượt đọc')
    so_luot_binh_luan = models.PositiveIntegerField(default=0, verbose_name='Số bình luận')
    so_luot_luu = models.PositiveIntegerField(default=0, verbose_name='Số lượt lưu')

    class Meta:
        verbose_name = 'Chương'
        verbose_name_plural = 'Chương'
        ordering = ['id']

    def save(self, *args, **kwargs):
        super().save(*args, **kwargs)
        if self.trang_thai == 'da_dang':
            self.truyen.save()

    def __str__(self):
        return f"{self.truyen.ten_truyen} - {self.tieu_de}"

# ==================== ĐÁNH GIÁ ====================
class DanhGia(models.Model):
    nguoi_dung = models.ForeignKey(NguoiDung, on_delete=models.CASCADE, verbose_name='Người dùng')
    truyen = models.ForeignKey(Truyen, on_delete=models.CASCADE, verbose_name='Truyện')
    sao_danh_gia = models.PositiveSmallIntegerField(choices=[(i, i) for i in range(1, 6)], verbose_name='Số sao')
    class Meta:
        verbose_name = 'Đánh giá'
        verbose_name_plural = 'Đánh giá'
        unique_together = ('nguoi_dung', 'truyen')

# ==================== BÌNH LUẬN ====================
class BinhLuan(models.Model):
    nguoi_dung = models.ForeignKey(NguoiDung, on_delete=models.CASCADE, verbose_name='Người dùng')
    truyen = models.ForeignKey(Truyen, on_delete=models.CASCADE, verbose_name='Truyện')
    noi_dung = models.TextField(verbose_name='Nội dung')
    thoi_gian_bl = models.DateTimeField(auto_now_add=True, verbose_name='Thời gian bình luận')
    class Meta:
        verbose_name = 'Bình luận'
        verbose_name_plural = 'Bình luận'

# ==================== THEO DÕI TRUYỆN ====================
class TheoDoiTruyen(models.Model):
    nguoi_dung = models.ForeignKey(NguoiDung, on_delete=models.CASCADE, verbose_name='Người dùng')
    truyen = models.ForeignKey(Truyen, on_delete=models.CASCADE, verbose_name='Truyện')
    class Meta:
        verbose_name = 'Theo dõi truyện'
        verbose_name_plural = 'Theo dõi truyện'
        unique_together = ('nguoi_dung', 'truyen')

# ==================== THEO DÕI NGƯỜI DÙNG ====================
class TheoDoiNguoiDung(models.Model):
    nguoi_theo_doi = models.ForeignKey(NguoiDung, on_delete=models.CASCADE, related_name='dang_theo_doi', verbose_name='Người theo dõi')
    nguoi_duoc_theo_doi = models.ForeignKey(NguoiDung, on_delete=models.CASCADE, related_name='nguoi_theo_doi_list', verbose_name='Người được theo dõi')
    class Meta:
        verbose_name = 'Theo dõi người dùng'
        verbose_name_plural = 'Theo dõi người dùng'
        unique_together = ('nguoi_theo_doi', 'nguoi_duoc_theo_doi')

# ==================== BỘ SƯU TẬP ====================
class BoSuuTap(models.Model):
    nguoi_dung = models.ForeignKey(NguoiDung, on_delete=models.CASCADE, related_name='bo_suu_tap_list', verbose_name='Chủ sở hữu')
    ten_bo_suu_tap = models.CharField(max_length=200, verbose_name='Tên bộ sưu tập')
    truyen_list = models.ManyToManyField(Truyen, through='BoSuuTapTruyen', related_name='bo_suu_tap_list', blank=True, verbose_name='Danh sách truyện')
    class Meta:
        verbose_name = 'Bộ sưu tập'
        verbose_name_plural = 'Bộ sưu tập'

# ==================== BỘ SƯU TẬP - TRUYỆN (BẢNG TRUNG GIAN) ====================
class BoSuuTapTruyen(models.Model):
    bo_suu_tap = models.ForeignKey(BoSuuTap, on_delete=models.CASCADE, verbose_name='Bộ sưu tập')
    truyen = models.ForeignKey(Truyen, on_delete=models.CASCADE, verbose_name='Truyện')
    class Meta:
        verbose_name = 'Bộ sưu tập - Truyện'
        verbose_name_plural = 'Bộ sưu tập - Truyện'
        unique_together = ('bo_suu_tap', 'truyen')

# ==================== LỊCH SỬ ĐỌC ====================
class LichSuDoc(models.Model):
    nguoi_dung = models.ForeignKey(NguoiDung, on_delete=models.CASCADE, related_name='lich_su_doc_list', verbose_name='Người dùng')
    truyen = models.ForeignKey(Truyen, on_delete=models.CASCADE, related_name='lich_su_doc_list', verbose_name='Truyện')
    chuong = models.ForeignKey(Chuong, on_delete=models.SET_NULL, null=True, blank=True, verbose_name='Chương đang đọc')
    updated_at = models.DateTimeField(auto_now=True, verbose_name='Cập nhật lần cuối')
    class Meta:
        verbose_name = 'Lịch sử đọc'
        verbose_name_plural = 'Lịch sử đọc'
        unique_together = ('nguoi_dung', 'truyen')
