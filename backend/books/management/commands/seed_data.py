# -*- coding: utf-8 -*-
"""
seed_data.py - Tạo dữ liệu mẫu có dấu tiếng Việt
Chạy: python manage.py seed_data
"""

from django.core.management.base import BaseCommand
from django.contrib.auth.models import User
from django.utils import timezone
from datetime import date, timedelta
import random

from books.models import (
    NguoiDung, Truyen, TheLoai, TheLoaiTruyen,
    Chuong, DanhGia, BinhLuan, TheoDoiTruyen,
    TheoDoiNguoiDung, BoSuuTap, BoSuuTapTruyen, LichSuDoc
)


class Command(BaseCommand):
    help = 'Tạo dữ liệu mẫu cho DocSachApp (có dấu tiếng Việt)'

    def handle(self, *args, **kwargs):
        self.stdout.write('=== Bắt đầu seed dữ liệu ===')
        self._clear_data()
        nguoi_dungs = self._seed_nguoi_dung()
        the_loais = self._seed_the_loai()
        truyens = self._seed_truyen(nguoi_dungs, the_loais)
        chuongs = self._seed_chuong(truyens)
        self._seed_danh_gia(nguoi_dungs, truyens)
        self._seed_binh_luan(nguoi_dungs, truyens)
        self._seed_theo_doi_truyen(nguoi_dungs, truyens)
        self._seed_theo_doi_nguoi_dung(nguoi_dungs)
        self._seed_bo_suu_tap(nguoi_dungs, truyens)
        self._seed_lich_su_doc(nguoi_dungs, truyens, chuongs)
        self.stdout.write(self.style.SUCCESS('=== Seed dữ liệu thành công! ==='))

    def _clear_data(self):
        LichSuDoc.objects.all().delete()
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
        self.stdout.write('  [OK] Đã xóa dữ liệu cũ')

    def _seed_nguoi_dung(self):
        users_data = [
            ('nguyenvana', 'vana@gmail.com', '1990-03-15', 'Thích đọc truyện fantasy và khoa học viễn tưởng'),
            ('tranthib', 'thib@gmail.com', '1995-07-22', 'Tác giả truyện ngôn tình, đam mê viết lách từ nhỏ'),
            ('levanc', 'vanc@gmail.com', '1988-11-05', 'Nhà văn nghiệp dư, chuyên viết truyện võ hiệp'),
            ('phamthid', 'thid@gmail.com', '2000-01-30', 'Sinh viên năm 3, thích đọc sách mỗi ngày'),
            ('hoangvane', 'vane@gmail.com', '1993-06-18', 'Đọc truyện mỗi tối trước khi ngủ'),
            ('vuthif', 'thif@gmail.com', '1997-09-25', 'Yêu thích thể loại kinh dị và trinh thám'),
            ('dangvang', 'vang@gmail.com', '1985-04-12', 'Giáo viên văn học, đọc sách là niềm đam mê'),
            ('buithih', 'thih@gmail.com', '2002-12-08', 'Học sinh cấp 3, mê truyện tranh và light novel'),
            ('nguyenthii', 'thii@gmail.com', '1991-08-17', 'Chuyên đọc truyện lịch sử và cổ đại'),
            ('lethij', 'thij@gmail.com', '1999-02-14', 'Fan cứng của dòng truyện hành động'),
            ('phanvank', 'vank@gmail.com', '1996-05-23', 'Viết blog về sách và review truyện'),
            ('maivanl', 'vanl@gmail.com', '1984-10-09', 'Thích truyện khoa học viễn tưởng'),
            ('dothinm', 'thinm@gmail.com', '2001-07-03', 'Người yêu văn học Việt Nam'),
            ('trinhthn', 'thn@gmail.com', '1994-03-27', 'Đọc truyện để giải trí sau giờ làm'),
            ('ngothio', 'thio@gmail.com', '1998-11-15', 'Tác giả có 5 năm kinh nghiệm viết truyện'),
            ('hoathip', 'thip@gmail.com', '1987-06-30', 'Mới bắt đầu viết truyện trên nền tảng'),
            ('luongvnq', 'vnq@gmail.com', '2003-04-20', 'Yêu thích truyện tình cảm lãng mạn'),
            ('trinhvnr', 'vnr@gmail.com', '1992-09-11', 'Đọc truyện từ hồi còn nhỏ, đam mê bất tận'),
            ('nguyenvns', 'vns@gmail.com', '1989-12-25', 'Học sinh, đọc truyện trên điện thoại mỗi ngày'),
            ('phantht', 'tht@gmail.com', '2004-08-07', 'Mê đọc truyện ngôn tình và xuyên không'),
            ('tranvnu', 'vnu@gmail.com', '1995-01-19', 'Nhà văn chuyên nghiệp, đã xuất bản 3 cuốn sách'),
            ('levnv', 'vnv@gmail.com', '1986-07-04', 'Đam mê sáng tác và chia sẻ truyện hay'),
        ]

        nguoi_dungs = []
        for username, email, ngay_sinh, mo_ta in users_data:
            user = User.objects.create_user(username=username, email=email, password='password123')
            nd = NguoiDung.objects.create(user=user, ngay_sinh=date.fromisoformat(ngay_sinh), mo_ta=mo_ta)
            nguoi_dungs.append(nd)

        self.stdout.write(f'  [OK] Đã tạo {len(nguoi_dungs)} người dùng')
        return nguoi_dungs

    def _seed_the_loai(self):
        ten_list = [
            'Hành động', 'Tình cảm', 'Fantasy', 'Khoa học viễn tưởng',
            'Kinh dị', 'Hài hước', 'Lịch sử', 'Võ hiệp', 'Trinh thám',
            'Phiêu lưu', 'Tâm lý', 'Kinh doanh', 'Học đường', 'Đô thị',
            'Xuyên không', 'Cổ đại', 'Ngôn tình', 'Trọng sinh', 'Tu tiên',
            'Dị năng', 'Thể thao', 'Âm nhạc',
        ]
        the_loais = []
        for ten in ten_list:
            tl = TheLoai.objects.create(ten_the_loai=ten)
            the_loais.append(tl)
        self.stdout.write(f'  [OK] Đã tạo {len(the_loais)} thể loại')
        return the_loais

    def _seed_truyen(self, nguoi_dungs, the_loais):
        truyen_data = [
            ('Kiếm Thần Vô Song', 'Câu chuyện về một thiếu niên luyện kiếm đến đỉnh cao thiên hạ, vượt qua muôn vàn thử thách để trở thành kiếm thần mạnh nhất.', 'da_dang'),
            ('Ngôn Tình Mùa Hè', 'Tình yêu bao la giữa hai người trẻ lạc lối trong thành phố nhộn nhịp, cùng nhau tìm lại ý nghĩa cuộc sống.', 'da_dang'),
            ('Xuyên Không Đến Thời Đại Nhà Đường', 'Cô gái hiện đại bất ngờ xuyên về triều đại nhà Đường, đối mặt với âm mưu cung đình và tìm đường trở về.', 'da_dang'),
            ('Thám Tử Hào Hiệp', 'Thám tử trẻ tuổi giải quyết những vụ án bí ẩn trong thành phố, phơi bày sự thật đằng sau mỗi tội ác.', 'da_dang'),
            ('Tu Tiên Vô Cực', 'Hành trình tu luyện của chàng trai trẻ trong thế giới tu tiên đầy nguy hiểm và cơ hội.', 'da_dang'),
            ('Học Đường Yêu Thương', 'Câu chuyện tình yêu trong sáng trong môi trường học đường, nơi tình bạn và tình yêu đan xen.', 'da_dang'),
            ('Rồng Và Phượng', 'Cuộc chiến giữa hai tộc Rồng và Phượng Hoàng, kéo theo vận mệnh của cả thiên hạ.', 'da_dang'),
            ('Bí Ẩn Lâu Đài Ma', 'Câu chuyện kinh dị xảy ra trong một lâu đài cổ bị nguyền rủa hàng trăm năm.', 'da_dang'),
            ('Đế Vương Tái Sinh', 'Đế vương bị phản bội đầu thai để trả thù, lần này ngài sẽ không để ai có thể phản bội.', 'da_dang'),
            ('Tình Yêu Thế Kỷ', 'Chuyện tình xuyên thế kỷ của hai tâm hồn, vượt qua thời gian và không gian.', 'hoan_thanh'),
            ('Ninja Thế Giới Ngầm', 'Cuộc phiêu lưu của một ninja trong thế giới ngầm đầy hiểm nguy và bí mật.', 'da_dang'),
            ('Siêu Nhân Vũ Trụ', 'Con người có siêu năng lực chiến đấu với ngoại hành tinh để bảo vệ Trái Đất.', 'da_dang'),
            ('Cung Đấu Triều Ca', 'Cuộc tranh giành quyền lực trong cung đình, nơi mỗi bước đi đều là một ván cờ sinh tử.', 'da_dang'),
            ('Bác Sĩ Thần Kỳ', 'Bác sĩ trẻ tài năng vượt qua mọi thách thức nghề nghiệp để cứu sống bệnh nhân.', 'da_dang'),
            ('Võ Lâm Đệ Nhất', 'Câu chuyện về đại hội võ lâm và tình yêu nảy nở giữa hai môn phái đối đầu.', 'hoan_thanh'),
            ('Ngũ Hành Linh Giới', 'Thế giới năm nguyên tố và những cuộc chiến thần kỳ giữa các phe phái.', 'da_dang'),
            ('Trường Học Phép Thuật', 'Học sinh học phép thuật trong một trường học bí ẩn, khám phá sức mạnh tiềm ẩn.', 'da_dang'),
            ('CEO Yêu Em', 'Cô gái bình thường kết duyên với CEO giàu có, vượt qua khoảng cách giai cấp.', 'da_dang'),
            ('Anh Hùng Tối Thượng', 'Chàng trai trở thành siêu anh hùng mạnh nhất thế giới, gánh vác trọng trách lớn lao.', 'da_dang'),
            ('Cổ Đại Phong Lưu', 'Cuộc sống phong lưu của nam chính thời cổ đại, giữa triều chính và giang hồ.', 'ban_thao'),
            ('Biển Trời Bao La', 'Cuộc phiêu lưu trên đại dương rộng lớn, tìm kiếm kho báu huyền thoại.', 'da_dang'),
            ('Từ Biên Giới Chiến', 'Câu chuyện về những người lính dũng cảm bảo vệ biên giới Tổ quốc.', 'da_dang'),
        ]

        truyens = []
        for i, (ten, mo_ta, trang_thai) in enumerate(truyen_data):
            nguoi_dung = nguoi_dungs[i % len(nguoi_dungs)]
            truyen = Truyen.objects.create(
                nguoi_dung=nguoi_dung, ten_truyen=ten, mo_ta=mo_ta,
                trang_thai=trang_thai, so_luot_doc=random.randint(100, 50000)
            )
            created = timezone.now() - timedelta(days=random.randint(1, 7) if i < 10 else random.randint(30, 180))
            Truyen.objects.filter(pk=truyen.pk).update(created_at=created)
            for tl in random.sample(the_loais, random.randint(1, 3)):
                TheLoaiTruyen.objects.get_or_create(truyen=truyen, the_loai=tl)
            truyens.append(truyen)

        self.stdout.write(f'  [OK] Đã tạo {len(truyens)} truyện')
        return truyens

    def _seed_chuong(self, truyens):
        noi_dung_mau = [
            'Nhân vật chính bước vào một thế giới hoàn toàn mới. Những thử thách và cơ hội đang chờ đón phía trước. '
            'Hành trình dài và đầy gian nan nhưng cũng không thiếu những khoảnh khắc đẹp. '
            'Tình bạn, tình yêu và lòng dũng cảm sẽ được thử thách qua từng trang sách.',

            'Ánh nắng chiều tà phủ lên thành phố một lớp vàng óng ả. Nhân vật chính đứng trên đỉnh tòa nhà, '
            'nhìn xuống con phố nhộn nhịp bên dưới. Một cuộc chiến mới sắp bắt đầu, và lần này, '
            'không có chỗ cho sự do dự hay yếu đuối.',

            'Trong căn phòng tối om, chỉ có ánh nến leo lét chiếu sáng. Những dòng chữ cổ xưa trên tấm bản đồ '
            'dần hiện ra, hé lộ bí mật đã bị chôn vùi hàng nghìn năm. Người nắm giữ bí mật này '
            'sẽ có sức mạnh thay đổi cả thế giới.',

            'Tiếng gió thổi vi vu qua khu rừng già. Đoàn người lặng lẽ tiến bước trong đêm tối, '
            'mỗi người mang trong lòng một nỗi niềm riêng. Con đường phía trước còn rất dài, '
            'nhưng họ biết rằng chỉ cần đoàn kết, không gì là không thể vượt qua.',
        ]

        total = 0
        all_chuongs = {}
        for idx, truyen in enumerate(truyens):
            so_chuong = random.randint(3, 6)
            chuong_list = []
            for j in range(so_chuong):
                trang_thai = 'da_dang' if truyen.trang_thai in ['da_dang', 'hoan_thanh'] else 'ban_thao'
                thoi_gian_dang = None
                if trang_thai == 'da_dang':
                    if idx >= 10 and j == so_chuong - 1:
                        thoi_gian_dang = timezone.now() - timedelta(days=random.randint(1, 3))
                    else:
                        thoi_gian_dang = timezone.now() - timedelta(days=random.randint(1, 60))

                nd = random.choice(noi_dung_mau)
                c = Chuong.objects.create(
                    truyen=truyen,
                    tieu_de=f'Chương {j + 1}: {"Khởi đầu mới" if j == 0 else "Hành trình tiếp diễn" if j == 1 else "Thử thách phía trước" if j == 2 else "Bí mật hé lộ" if j == 3 else "Đỉnh cao quyết chiến" if j == 4 else "Kết thúc và khởi đầu"}',
                    noi_dung=nd + f'\n\n[{truyen.ten_truyen} - Chương {j + 1}]',
                    trang_thai=trang_thai,
                    thoi_gian_dang=thoi_gian_dang
                )
                chuong_list.append(c)
                total += 1
            all_chuongs[truyen.id] = chuong_list

        self.stdout.write(f'  [OK] Đã tạo {total} chương')
        return all_chuongs

    def _seed_danh_gia(self, nguoi_dungs, truyens):
        count, pairs = 0, set()
        while count < 25:
            nd, tr = random.choice(nguoi_dungs), random.choice(truyens)
            if (nd.id, tr.id) not in pairs:
                pairs.add((nd.id, tr.id))
                DanhGia.objects.create(nguoi_dung=nd, truyen=tr, sao_danh_gia=random.randint(1, 5))
                count += 1
        self.stdout.write(f'  [OK] Đã tạo {count} đánh giá')

    def _seed_binh_luan(self, nguoi_dungs, truyens):
        noi_dungs = [
            'Truyện hay quá, mình đọc không thể dừng được!',
            'Tác giả viết rất có chiều sâu, nhân vật được xây dựng rất tốt.',
            'Mình chờ chương mới mỗi ngày, update nhanh lên nhé tác giả!',
            'Câu chuyện khá cuốn, nhưng có vài phần còn hơi chậm.',
            'Đoạn kết chương này quá hay! Hồi hộp ghê!',
            'Nhân vật chính mạnh nhưng không bị overpowered, cân bằng tốt.',
            'Truyện này mình giới thiệu cho bạn bè rồi, ai cũng mê.',
            'Plot twist ở chương 5 khiến mình bất ngờ hoàn toàn.',
            'Văn phong nhẹ nhàng, dễ đọc, phù hợp cho mọi lứa tuổi.',
            'Hy vọng tác giả ra thêm nhiều chương trong tuần này.',
            'Tôi thích cách tác giả mô tả cảnh vật, rất sống động.',
            'Nhân vật phụ cũng được phát triển tốt, không bị mờ nhạt.',
            'Đây là truyện hay nhất mình đọc năm nay!',
            'Mong tác giả đừng để truyện dở dang nhé!',
            'Đoạn đối thoại rất tự nhiên, không bị gượng ép.',
            'Thế giới trong truyện được xây dựng rất chi tiết và thú vị.',
            'Đọc một lần rồi đọc lại lần nữa vẫn thấy hay.',
            'Tác giả cập nhật đều, mình rất thích kiểu này.',
            'Câu chuyện có nhiều bất ngờ, không đoán được kết tiếp theo.',
            'Cảm ơn tác giả đã chia sẻ tác phẩm tuyệt vời này!',
            'Nhân vật nữ chính rất mạnh mẽ và độc lập, mình thích lắm.',
            'Mình đọc từ đầu đến cuối trong một ngày, hay không để đâu!',
        ]
        for i, noi_dung in enumerate(noi_dungs):
            BinhLuan.objects.create(
                nguoi_dung=nguoi_dungs[i % len(nguoi_dungs)],
                truyen=truyens[i % len(truyens)],
                noi_dung=noi_dung,
                thoi_gian_bl=timezone.now() - timedelta(hours=random.randint(1, 720))
            )
        self.stdout.write(f'  [OK] Đã tạo {len(noi_dungs)} bình luận')

    def _seed_theo_doi_truyen(self, nguoi_dungs, truyens):
        count, pairs = 0, set()
        while count < 25:
            nd, tr = random.choice(nguoi_dungs), random.choice(truyens)
            if (nd.id, tr.id) not in pairs:
                pairs.add((nd.id, tr.id))
                TheoDoiTruyen.objects.create(nguoi_dung=nd, truyen=tr)
                count += 1
        self.stdout.write(f'  [OK] Đã tạo {count} theo dõi truyện')

    def _seed_theo_doi_nguoi_dung(self, nguoi_dungs):
        count, pairs = 0, set()
        # User đầu tiên follow và được follow
        a = nguoi_dungs[0]
        for nd in nguoi_dungs[1:5]:
            TheoDoiNguoiDung.objects.get_or_create(nguoi_theo_doi=a, nguoi_duoc_theo_doi=nd)
            TheoDoiNguoiDung.objects.get_or_create(nguoi_theo_doi=nd, nguoi_duoc_theo_doi=a)
            count += 2
        # Random follows
        while count < 25:
            nd1, nd2 = random.choice(nguoi_dungs), random.choice(nguoi_dungs)
            if nd1 != nd2 and (nd1.id, nd2.id) not in pairs:
                pairs.add((nd1.id, nd2.id))
                TheoDoiNguoiDung.objects.get_or_create(nguoi_theo_doi=nd1, nguoi_duoc_theo_doi=nd2)
                count += 1
        self.stdout.write(f'  [OK] Đã tạo {count} theo dõi người dùng')

    def _seed_bo_suu_tap(self, nguoi_dungs, truyens):
        ten_bst = [
            'Yêu thích', 'Đọc sau', 'Đang đọc', 'Đã hoàn thành',
            'Truyện hay nhất', 'Bộ sưu tập cá nhân', 'Giữ lại đọc',
            'Gợi ý cho bạn bè', 'Đọc cuối tuần', 'Truyện tiên hiệp',
            'Truyện tình cảm', 'Truyện hành động', 'Truyện kinh dị',
            'Đọc trước khi ngủ', 'Truyện ngắn hay', 'Top truyện 2024',
            'Truyện mới update', 'Chờ ra chương mới', 'Đọc lại lần 2',
            'Chia sẻ với bạn', 'BST học đường', 'BST fantasy',
        ]
        count_bst, count_rel = 0, 0
        for i, ten in enumerate(ten_bst):
            bst = BoSuuTap.objects.create(nguoi_dung=nguoi_dungs[i % len(nguoi_dungs)], ten_bo_suu_tap=ten)
            for tr in random.sample(truyens, random.randint(2, 4)):
                BoSuuTapTruyen.objects.create(bo_suu_tap=bst, truyen=tr)
                count_rel += 1
            count_bst += 1
        self.stdout.write(f'  [OK] Đã tạo {count_bst} bộ sưu tập, {count_rel} BST-Truyện')

    def _seed_lich_su_doc(self, nguoi_dungs, truyens, all_chuongs):
        count = 0
        for nd in nguoi_dungs[:10]:
            read_truyens = random.sample(truyens, random.randint(2, 5))
            for tr in read_truyens:
                chuong = None
                if tr.id in all_chuongs and all_chuongs[tr.id]:
                    chuong = random.choice(all_chuongs[tr.id])
                LichSuDoc.objects.create(nguoi_dung=nd, truyen=tr, chuong=chuong)
                count += 1
        self.stdout.write(f'  [OK] Đã tạo {count} lịch sử đọc')