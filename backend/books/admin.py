"""
admin.py – Đăng ký tất cả models vào Django Admin
"""
from django.contrib import admin
from .models import (
    NguoiDung, Truyen, TheLoai, TheLoaiTruyen,
    Chuong, DanhGia, BinhLuan, TheoDoiTruyen,
    TheoDoiNguoiDung, BoSuuTap, BoSuuTapTruyen
)


@admin.register(NguoiDung)
class NguoiDungAdmin(admin.ModelAdmin):
    list_display = ['id', 'ten_dang_nhap', 'email', 'ngay_sinh']
    search_fields = ['user__username', 'user__email']


@admin.register(TheLoai)
class TheLoaiAdmin(admin.ModelAdmin):
    list_display = ['id', 'ten_the_loai']
    search_fields = ['ten_the_loai']


@admin.register(Truyen)
class TruyenAdmin(admin.ModelAdmin):
    list_display = ['id', 'ten_truyen', 'nguoi_dung', 'trang_thai', 'so_luot_doc']
    list_filter = ['trang_thai']
    search_fields = ['ten_truyen']


@admin.register(TheLoaiTruyen)
class TheLoaiTruyenAdmin(admin.ModelAdmin):
    list_display = ['id', 'truyen', 'the_loai']


@admin.register(Chuong)
class ChuongAdmin(admin.ModelAdmin):
    list_display = ['id', 'truyen', 'tieu_de', 'trang_thai', 'thoi_gian_tao']
    list_filter = ['trang_thai']


@admin.register(DanhGia)
class DanhGiaAdmin(admin.ModelAdmin):
    list_display = ['id', 'nguoi_dung', 'truyen', 'sao_danh_gia']


@admin.register(BinhLuan)
class BinhLuanAdmin(admin.ModelAdmin):
    list_display = ['id', 'nguoi_dung', 'truyen', 'thoi_gian_bl']


@admin.register(TheoDoiTruyen)
class TheoDoiTruyenAdmin(admin.ModelAdmin):
    list_display = ['id', 'nguoi_dung', 'truyen']


@admin.register(TheoDoiNguoiDung)
class TheoDoiNguoiDungAdmin(admin.ModelAdmin):
    list_display = ['id', 'nguoi_theo_doi', 'nguoi_duoc_theo_doi']


@admin.register(BoSuuTap)
class BoSuuTapAdmin(admin.ModelAdmin):
    list_display = ['id', 'nguoi_dung', 'ten_bo_suu_tap']


@admin.register(BoSuuTapTruyen)
class BoSuuTapTruyenAdmin(admin.ModelAdmin):
    list_display = ['id', 'bo_suu_tap', 'truyen']
