# -*- coding: utf-8 -*-
"""
seed_data.py - Management command de tao du lieu mau
Chay: python manage.py seed_data
"""

from django.core.management.base import BaseCommand
from django.contrib.auth.models import User
from django.utils import timezone
from datetime import date, timedelta
import random

from books.models import (
    NguoiDung, Truyen, TheLoai, TheLoaiTruyen,
    Chuong, DanhGia, BinhLuan, TheoDoiTruyen,
    TheoDoiNguoiDung, BoSuuTap, BoSuuTapTruyen
)


class Command(BaseCommand):
    help = 'Tao du lieu mau cho DocSachApp (>=20 ban ghi moi bang)'

    def handle(self, *args, **kwargs):
        self.stdout.write('=== Bat dau seed du lieu ===')

        self._clear_data()

        nguoi_dungs = self._seed_nguoi_dung()
        the_loais = self._seed_the_loai()
        truyens = self._seed_truyen(nguoi_dungs, the_loais)
        self._seed_chuong(truyens)
        self._seed_danh_gia(nguoi_dungs, truyens)
        self._seed_binh_luan(nguoi_dungs, truyens)
        self._seed_theo_doi_truyen(nguoi_dungs, truyens)
        self._seed_theo_doi_nguoi_dung(nguoi_dungs)
        self._seed_bo_suu_tap(nguoi_dungs, truyens)
        self._seed_follow_cho_test(nguoi_dungs)
        self.stdout.write(self.style.SUCCESS('=== Seed du lieu thanh cong! ==='))

    def _clear_data(self):
        BoSuuTapTruyen.objects.all().delete()
        BoSuuTap.objects.all().delete()
        TheoDoiNguoiDung.objects.all().delete()
        TheoDoiTruyen.objects.all().delete()
        BinhLuan.objects.all().delete()
        DanhGia.objects.all().delete()
        TheLoaiTruyen.objects.all().delete()
        Chuong.objects.all().delete()
        Truyen.objects.all().delete()
        NguoiDung.objects.all().delete()
        User.objects.filter(is_superuser=False).delete()
        TheLoai.objects.all().delete()
        self.stdout.write('  [OK] Da xoa du lieu cu')

    def _seed_nguoi_dung(self):
        users_data = [
            ('nguyenvana', 'vana@gmail.com', '1990-03-15'),
            ('tranthib', 'thib@gmail.com', '1995-07-22'),
            ('levanc', 'vanc@gmail.com', '1988-11-05'),
            ('phamthid', 'thid@gmail.com', '2000-01-30'),
            ('hoangvane', 'vane@gmail.com', '1993-06-18'),
            ('vuthif', 'thif@gmail.com', '1997-09-25'),
            ('dangvang', 'vang@gmail.com', '1985-04-12'),
            ('buithhh', 'thhh@gmail.com', '2002-12-08'),
            ('nguyenthii', 'thii@gmail.com', '1991-08-17'),
            ('tranthij', 'thij@gmail.com', '1999-02-14'),
            ('lethik', 'thik@gmail.com', '1996-05-23'),
            ('phanvanl', 'vanl@gmail.com', '1984-10-09'),
            ('maivanm', 'vanm@gmail.com', '2001-07-03'),
            ('dothinn', 'thinn@gmail.com', '1994-03-27'),
            ('trinhtho', 'tho@gmail.com', '1998-11-15'),
            ('ngothip', 'thip@gmail.com', '1987-06-30'),
            ('hoathiq', 'thiq@gmail.com', '2003-04-20'),
            ('luongvnr', 'vnr@gmail.com', '1992-09-11'),
            ('trinhths', 'ths@gmail.com', '1989-12-25'),
            ('nguyenvt', 'vt@gmail.com', '2004-08-07'),
            ('phanthiu', 'thiu@gmail.com', '1995-01-19'),
            ('tranvnv', 'vnv@gmail.com', '1986-07-04'),
        ]

        mo_ta_list = [
            'Thich doc truyen fantasy',
            'Tac gia truyen ngon tinh',
            'Dam me viet lach tu nho',
            'Sinh vien, thich doc sach',
            'Doc truyen moi toi truoc khi ngu',
            'Yeu thich the loai kinh di',
            'Nha van nghiep du',
            'Hoc sinh cap 3, me truyen tranh',
            'Chuyen doc truyen lich su',
            'Fan cung cua dong truyen hanh dong',
            'Viet blog ve sach',
            'Giao vien, doc sach moi ngay',
            'Thich truyen khoa hoc vien tuong',
            'Nguoi yeu van hoc',
            'Doc truyen de giai tri',
            'Tac gia co 5 nam kinh nghiem',
            'Moi bat dau viet truyen',
            'Yeu thich truyen tinh cam',
            'Doc truyen tu hoi con nho',
            'Hoc sinh, doc truyen tren dien thoai',
            'Me doc truyen ngon tinh',
            'Nha van chuyen nghiep',
        ]

        nguoi_dungs = []
        for i, (username, email, ngay_sinh) in enumerate(users_data):
            user = User.objects.create_user(
                username=username,
                email=email,
                password='password123'
            )
            nd = NguoiDung.objects.create(
                user=user,
                ngay_sinh=date.fromisoformat(ngay_sinh),
                mo_ta=mo_ta_list[i]
            )
            nguoi_dungs.append(nd)

        self.stdout.write(f'  [OK] Da tao {len(nguoi_dungs)} nguoi dung')
        return nguoi_dungs

    def _seed_the_loai(self):
        ten_list = [
            'Hanh dong', 'Tinh cam', 'Fantasy', 'Khoa hoc vien tuong',
            'Kinh di', 'Hai huoc', 'Lich su', 'Vo hiep', 'Trinh tham',
            'Phieu luu', 'Tam ly', 'Kinh doanh', 'Hoc duong', 'Do thi',
            'Xuyen khong', 'Co dai', 'Ngon tinh', 'Trong sinh', 'Tu tien',
            'Di nang', 'The thao', 'Am nhac',
        ]
        the_loais = []
        for ten in ten_list:
            tl = TheLoai.objects.create(ten_the_loai=ten)
            the_loais.append(tl)
        self.stdout.write(f'  [OK] Da tao {len(the_loais)} the loai')
        return the_loais

    def _seed_truyen(self, nguoi_dungs, the_loais):
        truyen_data = [
            ('Kiem Than Vo Song', 'Cau chuyen ve mot thieu nien luyen kiem den dinh cao thien ha.', 'da_dang'),
            ('Ngon Tinh Mua He', 'Tinh yeu bao mat giua hai nguoi tre lac loi.', 'da_dang'),
            ('Xuyen Khong Den Thoi Dai Nha Duong', 'Co gai hien dai xuyen ve trieu dai nha Duong.', 'da_dang'),
            ('Tham Tu Hao Hiep', 'Tham tu tre giai quyet nhung vu an bi an trong thanh pho.', 'da_dang'),
            ('Tu Tien Vo Cuc', 'Hanh trinh tu luyen cua chang trai tre trong the gioi tu tien.', 'da_dang'),
            ('Hoc Duong Yeu Thuong', 'Cau chuyen tinh yeu trong moi truong hoc duong.', 'da_dang'),
            ('Rong Va Phuong', 'Cuoc chien giua hai toc rong va phuong hoang.', 'da_dang'),
            ('Bi An Lau Dai Ma', 'Cau chuyen kinh di xay ra trong mot lau dai co.', 'da_dang'),
            ('De Vuong Tai Sinh', 'De vuong bi phan boi dau thai de tra thu.', 'da_dang'),
            ('Tinh Yeu The Ky', 'Chuyen tinh xuyen the ky cua hai tam hon.', 'hoan_thanh'),
            ('Ninja The Gioi Ngam', 'Cuoc phieu luu cua mot ninja trong the gioi ngam.', 'da_dang'),
            ('Sieu Nhan Vu Tru', 'Con nguoi co sieu nang luc chien dau voi ngoai hanh tinh.', 'da_dang'),
            ('Cung Dau Trieu Ca', 'Cuoc tranh gianh quyen luc trong cung dinh.', 'da_dang'),
            ('Bac Si Than Ky', 'Bac si tre tai nang vuot qua moi thach thuc nghe nghiep.', 'da_dang'),
            ('Vo Lam De Nhat', 'Cau chuyen ve dai hoi vo lam va tinh yeu.', 'hoan_thanh'),
            ('Ngu Hanh Linh Gioi', 'The gioi nam nguyen to va nhung cuoc chien than ky.', 'da_dang'),
            ('Truong Hoc Phep Thuat', 'Hoc sinh hoc phep thuat trong mot truong hoc bi an.', 'da_dang'),
            ('CEO Yeu Em', 'Co gai binh thuong ben duyen voi CEO giau co.', 'da_dang'),
            ('Anh Hung Toi Thuong', 'Chang trai tro thanh sieu anh hung manh nhat the gioi.', 'da_dang'),
            ('Co Dai Phong Luu', 'Cuoc song phong luu cua nam chinh thoi co dai.', 'ban_thao'),
            ('Bien Troi Bao La', 'Cuoc phieu luu tren dai duong rong lon.', 'da_dang'),
            ('Tu Bien Gioi Chien', 'Cau chuyen ve nhung nguoi linh bao ve bien gioi.', 'da_dang'),
        ]

        truyens = []
        for i, (ten, mo_ta, trang_thai) in enumerate(truyen_data):
            nguoi_dung = nguoi_dungs[i % len(nguoi_dungs)]
            truyen = Truyen.objects.create(
                nguoi_dung=nguoi_dung,
                ten_truyen=ten,
                mo_ta=mo_ta,
                trang_thai=trang_thai,
                so_luot_doc=random.randint(100, 50000)
            )
            if i < 10:
                created = timezone.now() - timedelta(days=random.randint(1, 7))
            else:
                created = timezone.now() - timedelta(days=random.randint(30, 180))
            Truyen.objects.filter(pk=truyen.pk).update(created_at=created)
            assigned_tl = random.sample(the_loais, random.randint(1, 3))
            for tl in assigned_tl:
                TheLoaiTruyen.objects.get_or_create(truyen=truyen, the_loai=tl)
            truyens.append(truyen)

        self.stdout.write(f'  [OK] Da tao {len(truyens)} truyen')
        return truyens

    def _seed_chuong(self, truyens):
        noi_dung_mau = (
            "Nhan vat chinh buoc vao mot the gioi hoan toan moi. "
            "Nhung thu thach va co hoi dang cho don phia truoc. "
            "Hanh trinh dai va day gian nan nhung cung khong thieu nhung khoanh khac dep. "
            "Tinh ban, tinh yeu va long dung cam se duoc thu thach qua tung trang."
        )

        total = 0
        for idx, truyen in enumerate(truyens):
            so_chuong = random.randint(3, 6)
            for j in range(so_chuong):
                trang_thai = 'da_dang' if truyen.trang_thai in ['da_dang', 'hoan_thanh'] else 'ban_thao'
                # Truyện index 10-19: vừa thêm chương mới gần đây
                if idx >= 10 and j == so_chuong - 1:
                    thoi_gian_dang = timezone.now() - timedelta(days=random.randint(1, 3))
                else:
                    thoi_gian_dang = (
                        timezone.now() - timedelta(days=random.randint(1, 60))
                        if trang_thai == 'da_dang' else None
                    )
                Chuong.objects.create(
                    truyen=truyen,
                    tieu_de=f'Chuong {j + 1}',
                    noi_dung=noi_dung_mau + f' [Chuong {j + 1} cua {truyen.ten_truyen}]',
                    trang_thai=trang_thai,
                    thoi_gian_dang=thoi_gian_dang
                )
                total += 1

        self.stdout.write(f'  [OK] Da tao {total} chuong')

    def _seed_danh_gia(self, nguoi_dungs, truyens):
        count = 0
        pairs = set()
        attempts = 0
        while count < 22 and attempts < 200:
            attempts += 1
            nd = random.choice(nguoi_dungs)
            tr = random.choice(truyens)
            key = (nd.id, tr.id)
            if key not in pairs:
                pairs.add(key)
                DanhGia.objects.create(
                    nguoi_dung=nd,
                    truyen=tr,
                    sao_danh_gia=random.randint(1, 5)
                )
                count += 1
        self.stdout.write(f'  [OK] Da tao {count} danh gia')

    def _seed_binh_luan(self, nguoi_dungs, truyens):
        noi_dungs = [
            'Truyen hay qua, minh doc khong the dung duoc!',
            'Tac gia viet rat co chieu sau, nhan vat duoc xay dung rat tot.',
            'Minh cho chuong moi moi ngay, update nhanh len nhe tac gia!',
            'Cau chuyen kha cuon, nhung co vai phan con hoi cham.',
            'Doan ket chuong nay qua hay! Hoi hop ghe!',
            'Nhan vat chinh manh nhung khong bi overpowered, can bang tot.',
            'Truyen nay minh gioi thieu cho ban be roi, ai cung me.',
            'Plot twist o chuong 5 khien minh bat ngo hoan toan.',
            'Van phong nhe nhang, de doc, phu hop cho moi lua tuoi.',
            'Hy vong tac gia ra them nhieu chuong trong tuan nay.',
            'Toi thich cach tac gia mo ta canh vat, rat song dong.',
            'Nhan vat phu cung duoc phat trien tot, khong bi mo nhat.',
            'Day la truyen hay nhat minh doc nam nay!',
            'Mong tac gia dung de truyen do dang nhe!',
            'Doan doi thoai rat tu nhien, khong bi guong ep.',
            'The gioi trong truyen duoc xay dung rat chi tiet va thu vi.',
            'Doc mot lan roi doc lai lan nua van thay hay.',
            'Tac gia cap nhat deu, minh rat thich kieu nay.',
            'Cau chuyen co nhieu bat ngo, khong doan duoc ket tiep theo.',
            'Cam on tac gia da chia se tac pham tuyet voi nay!',
            'Nhan vat nu chinh rat manh me va doc lap, minh thich lam.',
            'Minh doc tu dau den cuoi trong mot ngay, hay khong de dau!',
        ]
        count = 0
        for i, noi_dung in enumerate(noi_dungs):
            nd = nguoi_dungs[i % len(nguoi_dungs)]
            tr = truyens[i % len(truyens)]
            BinhLuan.objects.create(
                nguoi_dung=nd,
                truyen=tr,
                noi_dung=noi_dung,
                thoi_gian_bl=timezone.now() - timedelta(hours=random.randint(1, 720))
            )
            count += 1
        self.stdout.write(f'  [OK] Da tao {count} binh luan')

    def _seed_theo_doi_truyen(self, nguoi_dungs, truyens):
        count = 0
        pairs = set()
        attempts = 0
        while count < 22 and attempts < 200:
            attempts += 1
            nd = random.choice(nguoi_dungs)
            tr = random.choice(truyens)
            key = (nd.id, tr.id)
            if key not in pairs:
                pairs.add(key)
                TheoDoiTruyen.objects.create(nguoi_dung=nd, truyen=tr)
                count += 1
        self.stdout.write(f'  [OK] Da tao {count} theo doi truyen')

    def _seed_theo_doi_nguoi_dung(self, nguoi_dungs):
        count = 0
        pairs = set()
        attempts = 0
        while count < 22 and attempts < 300:
            attempts += 1
            nd1 = random.choice(nguoi_dungs)
            nd2 = random.choice(nguoi_dungs)
            if nd1 != nd2:
                key = (nd1.id, nd2.id)
                if key not in pairs:
                    pairs.add(key)
                    TheoDoiNguoiDung.objects.create(
                        nguoi_theo_doi=nd1,
                        nguoi_duoc_theo_doi=nd2
                    )
                    count += 1
        self.stdout.write(f'  [OK] Da tao {count} theo doi nguoi dung')

    def _seed_bo_suu_tap(self, nguoi_dungs, truyens):
        ten_bst = [
            'Yeu thich', 'Doc sau', 'Dang doc', 'Da hoan thanh',
            'Truyen hay nhat', 'Bo suu tap ca nhan', 'Giu lai doc',
            'Goi y cho ban be', 'Doc cuoi tuan', 'Truyen tien hiep',
            'Truyen tinh cam', 'Truyen hanh dong', 'Truyen kinh di',
            'Doc truoc khi ngu', 'Truyen ngan hay', 'Top truyen 2024',
            'Truyen moi update', 'Cho ra chuong moi', 'Doc lai lan 2',
            'Chia se voi ban', 'BST hoc duong', 'BST fantasy',
        ]
        count_bst = 0
        count_bst_truyen = 0
        bst_list = []
        for i, ten in enumerate(ten_bst):
            nd = nguoi_dungs[i % len(nguoi_dungs)]
            bst = BoSuuTap.objects.create(nguoi_dung=nd, ten_bo_suu_tap=ten)
            bst_list.append(bst)
            count_bst += 1

        pairs = set()
        for bst in bst_list:
            truyen_chon = random.sample(truyens, random.randint(2, 4))
            for tr in truyen_chon:
                key = (bst.id, tr.id)
                if key not in pairs:
                    pairs.add(key)
                    BoSuuTapTruyen.objects.create(bo_suu_tap=bst, truyen=tr)
                    count_bst_truyen += 1

        self.stdout.write(f'  [OK] Da tao {count_bst} bo suu tap, {count_bst_truyen} BST-Truyen')

    def _seed_follow_cho_test(self, nguoi_dungs):
        """Đảm bảo user đầu tiên (nguyenvana) có followers và following để test"""
        profile_a = nguoi_dungs[0]  # nguyenvana

        # nguyenvana follow 3 người
        for nd in nguoi_dungs[1:4]:
            TheoDoiNguoiDung.objects.get_or_create(
                nguoi_theo_doi=profile_a,
                nguoi_duoc_theo_doi=nd
            )

        # 3 người follow lại nguyenvana
        for nd in nguoi_dungs[1:4]:
            TheoDoiNguoiDung.objects.get_or_create(
                nguoi_theo_doi=nd,
                nguoi_duoc_theo_doi=profile_a
            )

        self.stdout.write(f'  [OK] Da tao follow test cho {profile_a.user.username}')