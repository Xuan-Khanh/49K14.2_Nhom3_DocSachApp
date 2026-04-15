"""
serializers.py – DocSachApp
============================
Tất cả serializers cho các models.
Chia thành các nhóm: Auth, User, Truyen, Chuong, BinhLuan, DanhGia, BoSuuTap, TheLoai
"""

from django.contrib.auth.models import User
from rest_framework import serializers
from .models import (
    NguoiDung, Truyen, TheLoai, TheLoaiTruyen,
    Chuong, DanhGia, BinhLuan, TheoDoiTruyen,
    TheoDoiNguoiDung, BoSuuTap, BoSuuTapTruyen, LichSuDoc
)


# =============================================
# AUTH SERIALIZERS
# =============================================

class DangKySerializer(serializers.Serializer):
    """Serializer đăng ký tài khoản mới"""
    username = serializers.CharField(max_length=150)
    email = serializers.EmailField()
    password = serializers.CharField(min_length=6, write_only=True)

    def validate_username(self, value):
        if User.objects.filter(username=value).exists():
            raise serializers.ValidationError("Tên đăng nhập đã tồn tại.")
        return value

    def validate_email(self, value):
        if User.objects.filter(email=value).exists():
            raise serializers.ValidationError("Email đã được sử dụng.")
        return value

    def create(self, validated_data):
        # Tạo Django User (password sẽ được hash tự động)
        user = User.objects.create_user(
            username=validated_data['username'],
            email=validated_data['email'],
            password=validated_data['password']
        )
        # Tạo profile NguoiDung liên kết
        NguoiDung.objects.create(user=user)
        return user


class DangNhapSerializer(serializers.Serializer):
    """Serializer đăng nhập"""
    username = serializers.CharField()
    password = serializers.CharField(write_only=True)


# =============================================
# USER / PROFILE SERIALIZERS
# =============================================

class NguoiDungSerializer(serializers.ModelSerializer):
    """Serializer profile người dùng (đầy đủ, dùng cho GET profile)"""
    username = serializers.CharField(source='user.username', read_only=True)
    email = serializers.CharField(source='user.email', read_only=True)
    so_truyen = serializers.SerializerMethodField()
    so_follower = serializers.SerializerMethodField()
    so_following = serializers.SerializerMethodField()

    class Meta:
        model = NguoiDung
        fields = [
            'id', 'username', 'email',
            'avatar', 'ngay_sinh', 'mo_ta',
            'so_truyen', 'so_follower', 'so_following'
        ]

    def get_so_truyen(self, obj):
        return obj.truyen_list.filter(trang_thai='da_dang').count()

    def get_so_follower(self, obj):
        return obj.nguoi_theo_doi_list.count()

    def get_so_following(self, obj):
        return obj.dang_theo_doi.count()


class NguoiDungCapNhatSerializer(serializers.ModelSerializer):
    """Serializer cập nhật profile (PUT)"""
    email = serializers.EmailField(required=False)

    class Meta:
        model = NguoiDung
        fields = ['avatar', 'ngay_sinh', 'mo_ta']

    def update(self, instance, validated_data):
        # Cập nhật email nếu có
        email = self.initial_data.get('email')
        if email:
            instance.user.email = email
            instance.user.save()
        return super().update(instance, validated_data)


class NguoiDungNgoanSerializer(serializers.ModelSerializer):
    """Serializer nhỏ gọn để nhúng vào các response khác (bình luận, truyện)"""
    username = serializers.CharField(source='user.username', read_only=True)

    class Meta:
        model = NguoiDung
        fields = ['id', 'username', 'avatar', 'mo_ta']


# =============================================
# THỂ LOẠI SERIALIZERS
# =============================================

class TheLoaiSerializer(serializers.ModelSerializer):
    class Meta:
        model = TheLoai
        fields = ['id', 'ten_the_loai']


# =============================================
# TRUYỆN SERIALIZERS
# =============================================

class TruyenSerializer(serializers.ModelSerializer):
    """Serializer truyện dạng danh sách (gọn)"""
    tac_gia = NguoiDungNgoanSerializer(source='nguoi_dung', read_only=True)
    the_loai = TheLoaiSerializer(many=True, read_only=True)
    the_loai_ids = serializers.ListField(
        child=serializers.IntegerField(),
        write_only=True,
        required=False,
        help_text="Danh sách ID thể loại"
    )
    diem_trung_binh = serializers.SerializerMethodField()
    tong_danh_gia = serializers.SerializerMethodField()
    so_chuong = serializers.SerializerMethodField()

    class Meta:
        model = Truyen
        fields = [
            'id', 'ten_truyen', 'mo_ta', 'trang_thai',
            'anh_bia', 'so_luot_doc',
            'tac_gia', 'the_loai', 'the_loai_ids',
            'diem_trung_binh', 'tong_danh_gia', 'so_chuong'
        ]
        read_only_fields = ['so_luot_doc']

    def get_diem_trung_binh(self, obj):
        return obj.diem_trung_binh()

    def get_tong_danh_gia(self, obj):
        return obj.tong_danh_gia()

    def get_so_chuong(self, obj):
        return obj.chuong_list.count()

    def validate_trang_thai(self, value):
        """Khi trang_thai = 'da_dang' hoặc 'hoan_thanh', cần có thể loại"""
        return value

    def validate(self, data):
        # Nếu không phải bản thảo, phải có ảnh bìa
        trang_thai = data.get('trang_thai', 'ban_thao')
        if trang_thai != 'ban_thao':
            # Kiểm tra instance update (PUT)
            instance = self.instance
            anh_bia = data.get('anh_bia') or (instance.anh_bia if instance else None)
            if not anh_bia:
                raise serializers.ValidationError(
                    {"anh_bia": "Cần có ảnh bìa khi đăng truyện."}
                )
        return data

    def create(self, validated_data):
        the_loai_ids = validated_data.pop('the_loai_ids', [])
        # nguoi_dung được gán từ view
        truyen = Truyen.objects.create(**validated_data)
        # Thêm thể loại
        for tl_id in the_loai_ids:
            try:
                tl = TheLoai.objects.get(id=tl_id)
                TheLoaiTruyen.objects.create(truyen=truyen, the_loai=tl)
            except TheLoai.DoesNotExist:
                pass
        return truyen

    def update(self, instance, validated_data):
        the_loai_ids = validated_data.pop('the_loai_ids', None)
        # Cập nhật truyện
        for attr, value in validated_data.items():
            setattr(instance, attr, value)
        instance.save()
        # Cập nhật thể loại nếu có
        if the_loai_ids is not None:
            TheLoaiTruyen.objects.filter(truyen=instance).delete()
            for tl_id in the_loai_ids:
                try:
                    tl = TheLoai.objects.get(id=tl_id)
                    TheLoaiTruyen.objects.create(truyen=instance, the_loai=tl)
                except TheLoai.DoesNotExist:
                    pass
        return instance


# =============================================
# CHƯƠNG SERIALIZERS
# =============================================

class ChuongSerializer(serializers.ModelSerializer):
    """Serializer cho chương"""
    truyen_id = serializers.IntegerField(write_only=True)
    ten_truyen = serializers.CharField(source='truyen.ten_truyen', read_only=True)

    class Meta:
        model = Chuong
        fields = [
            'id', 'truyen_id', 'ten_truyen',
            'tieu_de', 'noi_dung', 'trang_thai',
            'thoi_gian_tao', 'thoi_gian_dang'
        ]
        read_only_fields = ['thoi_gian_tao', 'thoi_gian_dang']

    def validate(self, data):
        """Không cho đăng chương nếu thiếu tiêu đề hoặc nội dung"""
        trang_thai = data.get('trang_thai', 'ban_thao')
        if trang_thai == 'da_dang':
            if not data.get('tieu_de', '').strip():
                raise serializers.ValidationError(
                    {"tieu_de": "Tiêu đề không được để trống khi đăng chương."}
                )
            if not data.get('noi_dung', '').strip():
                raise serializers.ValidationError(
                    {"noi_dung": "Nội dung không được để trống khi đăng chương."}
                )
        return data


class ChuongNgoanSerializer(serializers.ModelSerializer):
    """Serializer chương dạng gọn (dùng trong danh sách)"""
    class Meta:
        model = Chuong
        fields = ['id', 'tieu_de', 'trang_thai', 'thoi_gian_tao', 'thoi_gian_dang']


# =============================================
# BÌNH LUẬN SERIALIZERS
# =============================================

class BinhLuanSerializer(serializers.ModelSerializer):
    """Serializer bình luận kèm thông tin người dùng"""
    nguoi_dung_info = NguoiDungNgoanSerializer(source='nguoi_dung', read_only=True)
    truyen_id = serializers.IntegerField(write_only=True)

    class Meta:
        model = BinhLuan
        fields = [
            'id', 'truyen_id', 'nguoi_dung_info',
            'noi_dung', 'thoi_gian_bl'
        ]
        read_only_fields = ['thoi_gian_bl']

    def validate_noi_dung(self, value):
        if not value.strip():
            raise serializers.ValidationError("Nội dung bình luận không được để trống.")
        return value


# =============================================
# ĐÁNH GIÁ SERIALIZERS
# =============================================

class DanhGiaSerializer(serializers.ModelSerializer):
    """Serializer đánh giá sao"""
    nguoi_dung_info = NguoiDungNgoanSerializer(source='nguoi_dung', read_only=True)
    story_id = serializers.IntegerField(source='truyen_id', write_only=True)
    rating = serializers.IntegerField(source='sao_danh_gia', min_value=1, max_value=5)

    class Meta:
        model = DanhGia
        fields = ['id', 'story_id', 'nguoi_dung_info', 'rating', 'sao_danh_gia']
        read_only_fields = ['sao_danh_gia']

    def validate_rating(self, value):
        if value < 1 or value > 5:
            raise serializers.ValidationError("Số sao phải từ 1 đến 5.")
        return value


# =============================================
# THEO DÕI TRUYỆN SERIALIZERS
# =============================================

class TheoDoiTruyenSerializer(serializers.ModelSerializer):
    """Serializer truyện đang theo dõi"""
    truyen_info = TruyenSerializer(source='truyen', read_only=True)

    class Meta:
        model = TheoDoiTruyen
        fields = ['id', 'truyen_info']


# =============================================
# BỘ SƯU TẬP SERIALIZERS
# =============================================

class BoSuuTapSerializer(serializers.ModelSerializer):
    """Serializer bộ sưu tập (danh sách)"""
    so_truyen = serializers.SerializerMethodField()

    class Meta:
        model = BoSuuTap
        fields = ['id', 'ten_bo_suu_tap', 'so_truyen']

    def get_so_truyen(self, obj):
        return obj.truyen_list.count()


class BoSuuTapDetailSerializer(serializers.ModelSerializer):
    """Serializer bộ sưu tập kèm danh sách truyện"""
    truyen_list = TruyenSerializer(many=True, read_only=True)

    class Meta:
        model = BoSuuTap
        fields = ['id', 'ten_bo_suu_tap', 'truyen_list']


# =============================================
# LỊCH SỬ ĐỌC SERIALIZERS
# =============================================

class LichSuDocSerializer(serializers.ModelSerializer):
    """
    Serializer lịch sử đọc.
    Trả về thông tin đầy đủ để hiển thị UI:
      - id: ID bản ghi lịch sử
      - book_id: ID truyện  → dùng để navigate sang BookDetailsActivity
      - title: Tên truyện   → hiển thị tên
      - author: Tên tác giả → hiển thị tác giả
      - cover_url: URL ảnh bìa → load bằng Glide
      - views: Số lượt đọc
      - followers: Số người theo dõi
      - chapters_count: Tổng số chương
      - genres: Danh sách thể loại
      - current_chapter: Chương đang đọc (id + tên)
      - updated_at: Thời gian đọc lần cuối → sort, hiển thị
    """
    # Thông tin truyện
    book_id      = serializers.IntegerField(source='truyen.id', read_only=True)
    title        = serializers.CharField(source='truyen.ten_truyen', read_only=True)
    cover_url    = serializers.SerializerMethodField()
    views        = serializers.IntegerField(source='truyen.so_luot_doc', read_only=True)
    followers    = serializers.SerializerMethodField()
    chapters_count = serializers.SerializerMethodField()
    genres       = serializers.SerializerMethodField()

    # Thông tin tác giả
    author       = serializers.CharField(
                       source='truyen.nguoi_dung.user.username',
                       read_only=True
                   )

    # Chương đang đọc
    current_chapter = serializers.SerializerMethodField()

    class Meta:
        model = LichSuDoc
        fields = [
            'id',
            'book_id', 'title', 'author', 'cover_url',
            'views', 'followers', 'chapters_count', 'genres',
            'current_chapter',
            'updated_at',
        ]

    def get_cover_url(self, obj):
        """Trả về URL đầy đủ của ảnh bìa (hoặc None nếu chưa có)"""
        request = self.context.get('request')
        if obj.truyen.anh_bia and request:
            return request.build_absolute_uri(obj.truyen.anh_bia.url)
        return None

    def get_followers(self, obj):
        """Số người đang theo dõi truyện"""
        return obj.truyen.theo_doi_list.count()

    def get_chapters_count(self, obj):
        """Tổng số chương (đã đăng)"""
        return obj.truyen.chuong_list.filter(trang_thai='da_dang').count()

    def get_genres(self, obj):
        """Danh sách tên thể loại"""
        return list(obj.truyen.the_loai.values_list('ten_the_loai', flat=True))

    def get_current_chapter(self, obj):
        """Thông tin chương đang đọc (None nếu chưa đọc chương nào)"""
        if obj.chuong:
            return {
                'id': obj.chuong.id,
                'title': obj.chuong.tieu_de,
            }
        return None
