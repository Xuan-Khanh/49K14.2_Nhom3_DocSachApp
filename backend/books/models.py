"""
models.py – DocSachApp
"""

from django.db import models
from django.contrib.auth.models import User

# ==================== NGƯỜI DÙNG ====================
class NguoiDung(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE, related_name='nguoidung')
    avatar = models.ImageField(upload_to='avatars/', null=True, blank=True)
    ngay_sinh = models.DateField(null=True, blank=True)
    mo_ta = models.TextField(null=True, blank=True)

    def __str__(self):
        return self.user.username

# ==================== THỂ LOẠI ====================
class TheLoai(models.Model):
    ten_the_loai = models.CharField(max_length=100, unique=True)
    def __str__(self):
        return self.ten_the_loai

# ==================== TRUYỆN ====================
class Truyen(models.Model):
    TRANG_THAI_CHOICES = [
        ('ban_thao', 'Bản thảo'),
        ('da_dang', 'Đã Đăng'),
        ('hoan_thanh', 'Hoàn thành'),
    ]

    nguoi_dung = models.ForeignKey(NguoiDung, on_delete=models.CASCADE, related_name='truyen_list')
    ten_truyen = models.CharField(max_length=255)
    mo_ta = models.TextField(blank=True)
    trang_thai = models.CharField(max_length=20, choices=TRANG_THAI_CHOICES, default='ban_thao')
    anh_bia = models.ImageField(upload_to='covers/', null=True, blank=True)
    so_luot_doc = models.PositiveIntegerField(default=0)
    
    # THÊM 2 DÒNG NÀY ĐỂ QUẢN LÝ THỜI GIAN
    created_at = models.DateTimeField(auto_now_add=True, null=True, verbose_name='Ngày đăng')
    updated_at = models.DateTimeField(auto_now=True, null=True, verbose_name='Cập nhật mới nhất')

    the_loai = models.ManyToManyField(TheLoai, through='TheLoaiTruyen', related_name='truyen_list', blank=True)

    class Meta:
        ordering = ['-updated_at'] # Mặc định đẩy truyện mới cập nhật lên đầu

    def __str__(self):
        return self.ten_truyen

    def diem_trung_binh(self):
        danhgias = self.danh_gia_list.all()
        if not danhgias.exists(): return 0
        return round(sum(d.sao_danh_gia for d in danhgias) / danhgias.count(), 1)

    def tong_danh_gia(self):
        return self.danh_gia_list.count()

# ==================== TRUNG GIAN THỂ LOẠI ====================
class TheLoaiTruyen(models.Model):
    truyen = models.ForeignKey(Truyen, on_delete=models.CASCADE)
    the_loai = models.ForeignKey(TheLoai, on_delete=models.CASCADE)
    class Meta:
        unique_together = ('truyen', 'the_loai')

# ==================== CHƯƠNG ====================
class Chuong(models.Model):
    truyen = models.ForeignKey(Truyen, on_delete=models.CASCADE, related_name='chuong_list')
    tieu_de = models.CharField(max_length=255)
    noi_dung = models.TextField(blank=True)
    thoi_gian_tao = models.DateTimeField(auto_now_add=True)
    trang_thai = models.CharField(max_length=20, choices=[('ban_thao', 'Bản thảo'), ('da_dang', 'Đã đăng tải')], default='ban_thao')
    thoi_gian_dang = models.DateTimeField(null=True, blank=True)

    def save(self, *args, **kwargs):
        # Tự động cập nhật updated_at của Truyện khi có chương mới được lưu
        super().save(*args, **kwargs)
        if self.trang_thai == 'da_dang':
            self.truyen.save() # Việc save() truyện cha sẽ kích hoạt auto_now=True của updated_at

    def __str__(self):
        return f"{self.truyen.ten_truyen} - {self.tieu_de}"

# ==================== CÁC BẢNG KHÁC (GIỮ NGUYÊN) ====================
class DanhGia(models.Model):
    nguoi_dung = models.ForeignKey(NguoiDung, on_delete=models.CASCADE, related_name='danh_gia_list')
    truyen = models.ForeignKey(Truyen, on_delete=models.CASCADE, related_name='danh_gia_list')
    sao_danh_gia = models.PositiveSmallIntegerField()
    class Meta: unique_together = ('nguoi_dung', 'truyen')

class BinhLuan(models.Model):
    nguoi_dung = models.ForeignKey(NguoiDung, on_delete=models.CASCADE, related_name='binh_luan_list')
    truyen = models.ForeignKey(Truyen, on_delete=models.CASCADE, related_name='binh_luan_list')
    noi_dung = models.TextField()
    thoi_gian_bl = models.DateTimeField(auto_now_add=True)

class TheoDoiTruyen(models.Model):
    nguoi_dung = models.ForeignKey(NguoiDung, on_delete=models.CASCADE, related_name='theo_doi_truyen_list')
    truyen = models.ForeignKey(Truyen, on_delete=models.CASCADE, related_name='theo_doi_list')
    class Meta: unique_together = ('nguoi_dung', 'truyen')

class TheoDoiNguoiDung(models.Model):
    nguoi_theo_doi = models.ForeignKey(NguoiDung, on_delete=models.CASCADE, related_name='dang_theo_doi')
    nguoi_duoc_theo_doi = models.ForeignKey(NguoiDung, on_delete=models.CASCADE, related_name='nguoi_theo_doi_list')
    class Meta: unique_together = ('nguoi_theo_doi', 'nguoi_duoc_theo_doi')

class BoSuuTap(models.Model):
    nguoi_dung = models.ForeignKey(NguoiDung, on_delete=models.CASCADE, related_name='bo_suu_tap_list')
    ten_bo_suu_tap = models.CharField(max_length=200)
    truyen_list = models.ManyToManyField(Truyen, through='BoSuuTapTruyen', related_name='bo_suu_tap_list', blank=True)

class BoSuuTapTruyen(models.Model):
    bo_suu_tap = models.ForeignKey(BoSuuTap, on_delete=models.CASCADE)
    truyen = models.ForeignKey(Truyen, on_delete=models.CASCADE)
    class Meta: unique_together = ('bo_suu_tap', 'truyen')

class LichSuDoc(models.Model):
    nguoi_dung = models.ForeignKey(NguoiDung, on_delete=models.CASCADE, related_name='lich_su_doc_list')
    truyen = models.ForeignKey(Truyen, on_delete=models.CASCADE, related_name='lich_su_doc_list')
    chuong = models.ForeignKey(Chuong, on_delete=models.SET_NULL, null=True, blank=True)
    updated_at = models.DateTimeField(auto_now=True)
    class Meta: unique_together = ('nguoi_dung', 'truyen')
