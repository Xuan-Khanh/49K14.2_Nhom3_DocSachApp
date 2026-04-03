"""
permissions.py – DocSachApp
============================
Các custom permission để kiểm soát quyền truy cập API.
"""

from rest_framework.permissions import BasePermission, SAFE_METHODS


class IsOwnerOrReadOnly(BasePermission):
    """
    Quyền: chỉ chủ sở hữu mới được sửa/xóa.
    Người khác chỉ được đọc (GET, HEAD, OPTIONS).

    Cách dùng trong view:
        permission_classes = [IsAuthenticated, IsOwnerOrReadOnly]

    Object phải có trường 'nguoi_dung' trỏ đến NguoiDung.
    """
    message = "Bạn không có quyền thực hiện hành động này."

    def has_object_permission(self, request, view, obj):
        # Cho phép đọc với mọi người
        if request.method in SAFE_METHODS:
            return True

        # Ghi: chỉ cho phép chủ sở hữu
        # Lấy profile NguoiDung từ user đang đăng nhập
        try:
            nguoi_dung = request.user.nguoidung
        except Exception:
            return False

        return obj.nguoi_dung == nguoi_dung


class IsAuthorOfStory(BasePermission):
    """
    Quyền dành cho chương: chỉ tác giả của truyện mới được tạo/sửa/xóa chương.
    Object là Chuong (có trường truyen.nguoi_dung).
    """
    message = "Chỉ tác giả mới được thao tác trên chương này."

    def has_object_permission(self, request, view, obj):
        if request.method in SAFE_METHODS:
            return True
        try:
            nguoi_dung = request.user.nguoidung
        except Exception:
            return False
        return obj.truyen.nguoi_dung == nguoi_dung


class IsCommentOwnerOrAdmin(BasePermission):
    """
    Quyền xóa bình luận: chỉ người tạo bình luận hoặc admin.
    """
    message = "Bạn không có quyền xóa bình luận này."

    def has_object_permission(self, request, view, obj):
        if request.method in SAFE_METHODS:
            return True
        if request.user.is_staff:
            return True
        try:
            nguoi_dung = request.user.nguoidung
        except Exception:
            return False
        return obj.nguoi_dung == nguoi_dung
