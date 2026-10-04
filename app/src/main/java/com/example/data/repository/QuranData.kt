package com.example.data.repository

import com.example.data.model.Ayah
import com.example.data.model.JuzInfo
import com.example.data.model.Qari
import com.example.data.model.Surah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.example.R

object QuranData {

        val qariList = listOf(
        Qari("mishary", "Mishary Rashid Alafasy", "Kuwait", "https://server8.mp3quran.net/afs/", "https://images.unsplash.com/photo-1542838132-92c53300491e?w=300&q=80", "https://server8.mp3quran.net/afs/", R.drawable.alafasy),
        Qari("abdulbasit", "AbdulBaset AbdulSamad", "Egypt", "https://server7.mp3quran.net/basit/", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300&q=80", "https://server7.mp3quran.net/basit/", R.drawable.abdulbaset),
        Qari("maher", "Maher Al-Muaiqly", "Saudi Arabia", "https://server12.mp3quran.net/maher/", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300&q=80", "https://server12.mp3quran.net/maher/", R.drawable.maher),
        Qari("ghamdi", "Saad Al-Ghamdi", "Saudi Arabia", "https://server7.mp3quran.net/s_gmd/", "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=300&q=80", "https://server7.mp3quran.net/s_gmd/", R.drawable.ghamdi),
        Qari("sudais", "Abdul Rahman Al-Sudais", "Saudi Arabia", "https://server11.mp3quran.net/sds/", "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=300&q=80", "https://server11.mp3quran.net/sds/", R.drawable.sudais),
        Qari("shatri", "Abu Bakr Al-Shatri", "Saudi Arabia", "https://server11.mp3quran.net/shatri/", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=300&q=80", "https://server11.mp3quran.net/shatri/", R.drawable.shatri),
        Qari("shuraim", "Saud Al-Shuraim", "Saudi Arabia", "https://server7.mp3quran.net/shur/", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&q=80", "https://server7.mp3quran.net/shur/", R.drawable.shuraim),
        Qari("husary", "Mahmoud Khalil Al-Husary", "Egypt", "https://server13.mp3quran.net/husr/", "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=300&q=80", "https://server13.mp3quran.net/husr/", R.drawable.husary),
        Qari("dosari", "Yasser Al-Dosari", "Saudi Arabia", "https://server11.mp3quran.net/yasser/", "https://images.unsplash.com/photo-1501196354995-cbb51c65aaea?w=300&q=80", "https://server11.mp3quran.net/yasser/", R.drawable.dosari),
        Qari("rifai", "Hani Ar-Rifai", "Saudi Arabia", "https://server11.mp3quran.net/mrifai/", "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=300&q=80", "https://server11.mp3quran.net/mrifai/", R.drawable.rifai),
        Qari("nabil_rifai", "Nabil Al-Rifai", "Saudi Arabia", "https://server9.mp3quran.net/nabil/", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300&q=80", "https://server9.mp3quran.net/nabil/", R.drawable.nabil_al_rifai),
        Qari("minshawi", "Mohamed Siddiq Al-Minshawi", "Egypt", "https://server10.mp3quran.net/minsh/", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300&q=80", "https://server10.mp3quran.net/minsh/", R.drawable.mohamed_siddiq_al_minshawi),
        Qari("tablawi", "Mohammad Al-Tablaway", "Egypt", "https://server12.mp3quran.net/tblawi/", "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=300&q=80", "https://server12.mp3quran.net/tblawi/", R.drawable.mohammad_al_tablaway),
        Qari("ajamy", "Ahmed Al-Ajamy", "Saudi Arabia", "https://server10.mp3quran.net/ajm/", "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=300&q=80", "https://server10.mp3quran.net/ajm/", R.drawable.ahmed_al_ajamy),
        Qari("hudhaify", "Ali Al-Hudhaify", "Saudi Arabia", "https://server9.mp3quran.net/hthfi/", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=300&q=80", "https://server9.mp3quran.net/hthfi/", R.drawable.ali_al_hudhaify),
        Qari("yamani", "Wadi' Al-Yamani", "Yemen", "https://server6.mp3quran.net/wdee3/", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&q=80", "https://server6.mp3quran.net/wdee3/", R.drawable.wadi_al_yamani),
        Qari("ossi", "Abdur Rahman Al-Ossi", "Saudi Arabia", "https://server6.mp3quran.net/aloosi/", "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=300&q=80", "https://server6.mp3quran.net/aloosi/", R.drawable.abdur_rahman_al_ossi),
        Qari("mustafa_ismail", "Mustafa Ismail", "Egypt", "https://server8.mp3quran.net/mustafa/", "https://images.unsplash.com/photo-1501196354995-cbb51c65aaea?w=300&q=80", "https://server8.mp3quran.net/mustafa/", R.drawable.mustafa_ismail),
        Qari("suwayid", "Abdulrahman Al-Suwayid", "Saudi Arabia", "https://server16.mp3quran.net/a_swaiyd/", "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=300&q=80", "https://server16.mp3quran.net/a_swaiyd/", R.drawable.abdulrahman_al_suwayid),
        Qari("alzain", "Alzain Mohamed Ahmed", "Sudan", "https://server9.mp3quran.net/alzain/", "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=300&q=80", "https://server9.mp3quran.net/alzain/", R.drawable.alzain_mohamed_ahmed),
        Qari("jibreel", "Muhammad Jibreel", "Egypt", "https://server8.mp3quran.net/jbrl/", "https://images.unsplash.com/photo-1521119989659-a83eee488004?w=300&q=80", "https://server8.mp3quran.net/jbrl/", R.drawable.muhammad_jibreel),
        Qari("akdar", "Ibrahim Al-Akdar", "Saudi Arabia", "https://server6.mp3quran.net/akdr/", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=300&q=80", "https://server6.mp3quran.net/akdr/", R.drawable.ibrahim_al_akdar),
        Qari("banna", "Mahmoud Ali Al-Banna", "Egypt", "https://server8.mp3quran.net/bna/", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=300&q=80", "https://server8.mp3quran.net/bna/", R.drawable.mahmoud_ali_al_banna),
        Qari("lahoni", "Mustafa Al-Lahoni", "Egypt", "https://server6.mp3quran.net/lahoni/", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300&q=80", "https://server6.mp3quran.net/lahoni/", R.drawable.mustafa_al_lahoni),
        Qari("qatam", "Nasser Al-Qatami", "Saudi Arabia", "https://server6.mp3quran.net/qtm/", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300&q=80", "https://server6.mp3quran.net/qtm/", R.drawable.nasser_al_qatami),
        Qari("barrak", "Mohammad Al-Barrak", "Kuwait", "https://server13.mp3quran.net/braak/", "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=300&q=80", "https://server13.mp3quran.net/braak/", R.drawable.mohammad_al_barrak),
        Qari("basafr", "Abdullah Basfar", "Saudi Arabia", "https://server6.mp3quran.net/bsfr/", "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=300&q=80", "https://server6.mp3quran.net/bsfr/", R.drawable.abdullah_basfar),
        Qari("abkar", "Idrees Abkar", "Saudi Arabia", "https://server6.mp3quran.net/abkr/", "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=300&q=80", "https://server6.mp3quran.net/abkr/", R.drawable.idrees_abkar),
        Qari("ayyoub", "Muhammad Ayyub", "Saudi Arabia", "https://server16.mp3quran.net/ayyoub2/", "https://images.unsplash.com/photo-1501196354995-cbb51c65aaea?w=300&q=80", "https://server16.mp3quran.net/ayyoub2/", R.drawable.muhammad_ayyub),
        Qari("budair", "Salah Al-Budair", "Saudi Arabia", "https://server6.mp3quran.net/s_bud/", "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=300&q=80", "https://server6.mp3quran.net/s_bud/", R.drawable.salah_al_budair),
        Qari("mossad", "Abdul Rahman Mossad", "Egypt", "https://server9.mp3quran.net/mossad/", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300&q=80", "https://server9.mp3quran.net/mossad/", R.drawable.abdul_rahman_mossad, recordedSurahs = listOf(1, 36, 55, 67, 73, 78, 79)),
        Qari("salimi", "Mansour Al-Salimi", "Saudi Arabia", "https://server14.mp3quran.net/mansor/", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300&q=80", "https://server14.mp3quran.net/mansor/", R.drawable.mansour_al_salimi, recordedSurahs = null)
    )
    val surahs: List<Surah> = listOf(
        Surah(1, "الفاتحة", "Al-Fatiha", "The Opening", "Meccan", 7, 1),
        Surah(2, "البقرة", "Al-Baqarah", "The Cow", "Medinan", 286, 1),
        Surah(3, "آل عمران", "Ali 'Imran", "Family of Imran", "Medinan", 200, 3),
        Surah(4, "النساء", "An-Nisa", "The Women", "Medinan", 176, 4),
        Surah(5, "المائدة", "Al-Ma'idah", "The Table Spread", "Medinan", 120, 6),
        Surah(6, "الأنعام", "Al-An'am", "The Cattle", "Meccan", 165, 7),
        Surah(7, "الأعراف", "Al-A'raf", "The Heights", "Meccan", 206, 8),
        Surah(8, "الأنفال", "Al-Anfal", "The Spoils of War", "Medinan", 75, 9),
        Surah(9, "التوبة", "At-Tawbah", "The Repentance", "Medinan", 129, 10),
        Surah(10, "يونس", "Yunus", "Jonah", "Meccan", 109, 11),
        Surah(11, "هود", "Hud", "Hud", "Meccan", 123, 11),
        Surah(12, "يوسف", "Yusuf", "Joseph", "Meccan", 111, 12),
        Surah(13, "الرعد", "Ar-Ra'd", "The Thunder", "Medinan", 43, 13),
        Surah(14, "إبراهيم", "Ibrahim", "Abraham", "Meccan", 52, 13),
        Surah(15, "الحجر", "Al-Hijr", "The Rocky Tract", "Meccan", 99, 14),
        Surah(16, "النحل", "An-Nahl", "The Bee", "Meccan", 128, 14),
        Surah(17, "الإسراء", "Al-Isra", "The Night Journey", "Meccan", 111, 15),
        Surah(18, "الكهف", "Al-Kahf", "The Cave", "Meccan", 110, 15),
        Surah(19, "مريم", "Maryam", "Mary", "Meccan", 98, 16),
        Surah(20, "طه", "Taha", "Ta-Ha", "Meccan", 135, 16),
        Surah(21, "الأنبياء", "Al-Anbiya", "The Prophets", "Meccan", 112, 17),
        Surah(22, "الحج", "Al-Hajj", "The Pilgrimage", "Medinan", 78, 17),
        Surah(23, "المؤمنون", "Al-Mu'minun", "The Believers", "Meccan", 118, 18),
        Surah(24, "النور", "An-Nur", "The Light", "Medinan", 64, 18),
        Surah(25, "الفرقان", "Al-Furqan", "The Criterion", "Meccan", 77, 18),
        Surah(26, "الشعراء", "Ash-Shu'ara", "The Poets", "Meccan", 227, 19),
        Surah(27, "النمل", "An-Naml", "The Ant", "Meccan", 93, 19),
        Surah(28, "القصص", "Al-Qasas", "The Stories", "Meccan", 88, 20),
        Surah(29, "العنكبوت", "Al-'Ankabut", "The Spider", "Meccan", 69, 20),
        Surah(30, "الروم", "Ar-Rum", "The Romans", "Meccan", 60, 21),
        Surah(31, "لقمان", "Luqman", "Luqman", "Meccan", 34, 21),
        Surah(32, "السجدة", "As-Sajdah", "The Prostration", "Meccan", 30, 21),
        Surah(33, "الأحزاب", "Al-Ahzab", "The Combined Forces", "Medinan", 73, 21),
        Surah(34, "سبإ", "Saba", "Sheba", "Meccan", 54, 22),
        Surah(35, "فاطر", "Fatir", "Originator", "Meccan", 45, 22),
        Surah(36, "يس", "Ya-Sin", "Ya-Sin", "Meccan", 83, 22),
        Surah(37, "الصافات", "As-Saffat", "Those Who Set The Ranks", "Meccan", 182, 23),
        Surah(38, "ص", "Sad", "Sad", "Meccan", 88, 23),
        Surah(39, "الزمر", "Az-Zumar", "The Troops", "Meccan", 75, 23),
        Surah(40, "غافر", "Ghafir", "The Forgiver", "Meccan", 85, 24),
        Surah(41, "فصلت", "Fussilat", "Explained In Detail", "Meccan", 54, 24),
        Surah(42, "الشورى", "Ash-Shura", "The Consultation", "Meccan", 53, 25),
        Surah(43, "الزخرف", "Az-Zukhruf", "The Ornaments of Gold", "Meccan", 89, 25),
        Surah(44, "الدخان", "Ad-Dukhan", "The Smoke", "Meccan", 59, 25),
        Surah(45, "الجاثية", "Al-Jathiyah", "The Crouching", "Meccan", 37, 25),
        Surah(46, "الأحقاف", "Al-Ahqaf", "The Wind-Curved Sandhills", "Meccan", 35, 26),
        Surah(47, "محمد", "Muhammad", "Muhammad", "Medinan", 38, 26),
        Surah(48, "الفتح", "Al-Fath", "The Victory", "Medinan", 29, 26),
        Surah(49, "الحجرات", "Al-Hujurat", "The Dwellings", "Medinan", 18, 26),
        Surah(50, "ق", "Qaf", "Qaf", "Meccan", 45, 26),
        Surah(51, "الذاريات", "Adh-Dhariyat", "The Winnowing Winds", "Meccan", 60, 26),
        Surah(52, "الطور", "At-Tur", "The Mount", "Meccan", 49, 27),
        Surah(53, "النجم", "An-Najm", "The Star", "Meccan", 62, 27),
        Surah(54, "القمر", "Al-Qamar", "The Moon", "Meccan", 55, 27),
        Surah(55, "الرحمن", "Ar-Rahman", "The Beneficent", "Medinan", 78, 27),
        Surah(56, "الواقعة", "Al-Waqi'ah", "The Inevitable", "Meccan", 96, 27),
        Surah(57, "الحديد", "Al-Hadid", "The Iron", "Medinan", 29, 27),
        Surah(58, "المجادلة", "Al-Mujadila", "The Pleading Woman", "Medinan", 22, 28),
        Surah(59, "الحشر", "Al-Hashr", "The Exile", "Medinan", 24, 28),
        Surah(60, "الممتحنة", "Al-Mumtahanah", "She That Is To Be Examined", "Medinan", 13, 28),
        Surah(61, "الصف", "As-Saff", "The Ranks", "Medinan", 14, 28),
        Surah(62, "الجمعة", "Al-Jumu'ah", "Friday", "Medinan", 11, 28),
        Surah(63, "المنافقون", "Al-Munafiqun", "The Hypocrites", "Medinan", 11, 28),
        Surah(64, "التغابن", "At-Taghabun", "The Mutual Disillusion", "Medinan", 18, 28),
        Surah(65, "الطلاق", "At-Talaq", "The Divorce", "Medinan", 12, 28),
        Surah(66, "التحريم", "At-Tahrim", "The Prohibition", "Medinan", 12, 28),
        Surah(67, "الملك", "Al-Mulk", "The Sovereignty", "Meccan", 30, 29),
        Surah(68, "القلم", "Al-Qalam", "The Pen", "Meccan", 52, 29),
        Surah(69, "الحاقة", "Al-Haqqah", "The Inevitable Reality", "Meccan", 52, 29),
        Surah(70, "المعارج", "Al-Ma'arij", "The Ascending Stairways", "Meccan", 44, 29),
        Surah(71, "نوح", "Nuh", "Noah", "Meccan", 28, 29),
        Surah(72, "الجن", "Al-Jinn", "The Jinn", "Meccan", 28, 29),
        Surah(73, "المزمل", "Al-Muzzammil", "The Enshrouded One", "Meccan", 20, 29),
        Surah(74, "المدثر", "Al-Muddaththir", "The Cloaked One", "Meccan", 56, 29),
        Surah(75, "القيامة", "Al-Qiyamah", "The Resurrection", "Meccan", 40, 29),
        Surah(76, "الإنسان", "Al-Insan", "Man", "Medinan", 31, 29),
        Surah(77, "المرسلات", "Al-Mursalat", "The Emissaries", "Meccan", 50, 29),
        Surah(78, "النبإ", "An-Naba", "The Tidings", "Meccan", 40, 30),
        Surah(79, "النازعات", "An-Nazi'at", "Those Who Drag Forth", "Meccan", 46, 30),
        Surah(80, "عبس", "'Abasa", "He Frowned", "Meccan", 42, 30),
        Surah(81, "التكوير", "At-Takwir", "The Overthrowing", "Meccan", 29, 30),
        Surah(82, "الانفطار", "Al-Infitar", "The Cleaving", "Meccan", 19, 30),
        Surah(83, "المطففين", "Al-Mutaffifin", "Defrauding", "Meccan", 36, 30),
        Surah(84, "الانشقاق", "Al-Inshiqaq", "The Splitting Open", "Meccan", 25, 30),
        Surah(85, "البروج", "Al-Buruj", "The Mansions of the Stars", "Meccan", 22, 30),
        Surah(86, "الطارق", "At-Tariq", "The Morning Star", "Meccan", 17, 30),
        Surah(87, "الأعلى", "Al-A'la", "The Most High", "Meccan", 19, 30),
        Surah(88, "الغاشية", "Al-Ghashiyah", "The Overwhelming", "Meccan", 26, 30),
        Surah(89, "الفجر", "Al-Fajr", "The Dawn", "Meccan", 30, 30),
        Surah(90, "البلد", "Al-Balad", "The City", "Meccan", 20, 30),
        Surah(91, "الشمس", "Ash-Shams", "The Sun", "Meccan", 15, 30),
        Surah(92, "الليل", "Al-Layl", "The Night", "Meccan", 21, 30),
        Surah(93, "الضحى", "Ad-Duha", "The Morning Hours", "Meccan", 11, 30),
        Surah(94, "الشرح", "Ash-Sharh", "The Relief", "Meccan", 8, 30),
        Surah(95, "التين", "At-Tin", "The Fig", "Meccan", 8, 30),
        Surah(96, "العلق", "Al-'Alaq", "The Clot", "Meccan", 19, 30),
        Surah(97, "القدر", "Al-Qadr", "The Power", "Meccan", 5, 30),
        Surah(98, "البينة", "Al-Bayyinah", "The Clear Proof", "Medinan", 8, 30),
        Surah(99, "الزلزلة", "Az-Zalzalah", "The Earthquake", "Medinan", 8, 30),
        Surah(100, "العاديات", "Al-'Adiyat", "The Courser", "Meccan", 11, 30),
        Surah(101, "القارعة", "Al-Qari'ah", "The Calamity", "Meccan", 11, 30),
        Surah(102, "التكاثر", "At-Takathur", "Rivalry In World Increase", "Meccan", 8, 30),
        Surah(103, "العصر", "Al-'Asr", "The Declining Day", "Meccan", 3, 30),
        Surah(104, "الهمزة", "Al-Humazah", "The Traducer", "Meccan", 9, 30),
        Surah(105, "الفيل", "Al-Fil", "The Elephant", "Meccan", 5, 30),
        Surah(106, "قريش", "Quraysh", "Quraysh", "Meccan", 4, 30),
        Surah(107, "الماعون", "Al-Ma'un", "Small Kindnesses", "Meccan", 7, 30),
        Surah(108, "الكوثر", "Al-Kawthar", "Abundance", "Meccan", 3, 30),
        Surah(109, "الكافرون", "Al-Kafirun", "The Disbelievers", "Meccan", 6, 30),
        Surah(110, "النصر", "An-Nasr", "The Divine Support", "Medinan", 3, 30),
        Surah(111, "المسد", "Al-Masad", "The Palm Fiber", "Meccan", 5, 30),
        Surah(112, "الإخلاص", "Al-Ikhlas", "The Sincerity", "Meccan", 4, 30),
        Surah(113, "الفلق", "Al-Falaq", "The Daybreak", "Meccan", 5, 30),
        Surah(114, "الناس", "An-Nas", "Mankind", "Meccan", 6, 30)
    )

    val juzList: List<JuzInfo> = listOf(
        JuzInfo(1, "الجزء الأول", "Al-Fatiha", 1, "Al-Baqarah", 141, listOf(1, 2)),
        JuzInfo(2, "الجزء الثاني", "Al-Baqarah", 142, "Al-Baqarah", 252, listOf(2)),
        JuzInfo(3, "الجزء الثالث", "Al-Baqarah", 253, "Ali 'Imran", 92, listOf(2, 3)),
        JuzInfo(4, "الجزء الرابع", "Ali 'Imran", 93, "An-Nisa", 23, listOf(3, 4)),
        JuzInfo(5, "الجزء الخامس", "An-Nisa", 24, "An-Nisa", 147, listOf(4)),
        JuzInfo(6, "الجزء السادس", "An-Nisa", 148, "Al-Ma'idah", 81, listOf(4, 5)),
        JuzInfo(7, "الجزء السابع", "Al-Ma'idah", 82, "Al-An'am", 110, listOf(5, 6)),
        JuzInfo(8, "الجزء الثامن", "Al-An'am", 111, "Al-A'raf", 87, listOf(6, 7)),
        JuzInfo(9, "الجزء التاسع", "Al-A'raf", 88, "Al-Anfal", 40, listOf(7, 8)),
        JuzInfo(10, "الجزء العاشر", "Al-Anfal", 41, "At-Tawbah", 92, listOf(8, 9)),
        JuzInfo(11, "الجزء الحادي عشر", "At-Tawbah", 93, "Hud", 5, listOf(9, 10, 11)),
        JuzInfo(12, "الجزء الثاني عشر", "Hud", 6, "Yusuf", 52, listOf(11, 12)),
        JuzInfo(13, "الجزء الثالث عشر", "Yusuf", 53, "Ibrahim", 52, listOf(12, 13, 14)),
        JuzInfo(14, "الجزء الرابع عشر", "Al-Hijr", 1, "An-Nahl", 128, listOf(15, 16)),
        JuzInfo(15, "الجزء الخامس عشر", "Al-Isra", 1, "Al-Kahf", 74, listOf(17, 18)),
        JuzInfo(16, "الجزء السادس عشر", "Al-Kahf", 75, "Taha", 135, listOf(18, 19, 20)),
        JuzInfo(17, "الجزء السابع عشر", "Al-Anbiya", 1, "Al-Hajj", 78, listOf(21, 22)),
        JuzInfo(18, "الجزء الثامن عشر", "Al-Mu'minun", 1, "Al-Furqan", 20, listOf(23, 24, 25)),
        JuzInfo(19, "الجزء التاسع عشر", "Al-Furqan", 21, "An-Naml", 55, listOf(25, 26, 27)),
        JuzInfo(20, "الجزء العشرون", "An-Naml", 56, "Al-'Ankabut", 45, listOf(27, 28, 29)),
        JuzInfo(21, "الجزء الحادي والعشرون", "Al-'Ankabut", 46, "Al-Ahzab", 30, listOf(29, 30, 31, 32, 33)),
        JuzInfo(22, "الجزء الثاني والعشرون", "Al-Ahzab", 31, "Yasin", 27, listOf(33, 34, 35, 36)),
        JuzInfo(23, "الجزء الثالث والعشرون", "Yasin", 28, "Az-Zumar", 31, listOf(36, 37, 38, 39)),
        JuzInfo(24, "الجزء الرابع والعشرون", "Az-Zumar", 32, "Fussilat", 46, listOf(39, 40, 41)),
        JuzInfo(25, "الجزء الخامس والعشرون", "Fussilat", 47, "Al-Jathiyah", 37, listOf(41, 42, 43, 44, 45)),
        JuzInfo(26, "الجزء السادس والعشرون", "Al-Ahqaf", 1, "Adh-Dhariyat", 30, listOf(46, 47, 48, 49, 50, 51)),
        JuzInfo(27, "الجزء السابع والعشرون", "Adh-Dhariyat", 31, "Al-Hadid", 29, listOf(51, 52, 53, 54, 55, 56, 57)),
        JuzInfo(28, "الجزء الثامن والعشرون", "Al-Mujadila", 1, "At-Tahrim", 12, listOf(58, 59, 60, 61, 62, 63, 64, 65, 66)),
        JuzInfo(29, "الجزء التاسع والعشرون", "Al-Mulk", 1, "Al-Mursalat", 50, listOf(67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77)),
        JuzInfo(30, "الجزء الثلاثون", "An-Naba", 1, "An-Nas", 6, listOf(78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114))
    )




    private val allAyahsCache = java.util.concurrent.ConcurrentHashMap<String, List<Ayah>>()

    /** Non-suspending cache peek so screens can render instantly on re-visits. */
    fun cachedAyahsForSurah(surahId: Int, langId: String = "en"): List<Ayah>? =
        allAyahsCache["$surahId-$langId"]

    /** O(1) surah lookup used by the hot rendering paths. */
    val surahsById: Map<Int, Surah> by lazy { surahs.associateBy { it.id } }

    fun surahById(surahId: Int): Surah? = surahsById[surahId]

    private fun getTranslationEndpoint(langId: String): String {
        return when (langId) {
            "en" -> "en.asad"
            "ur" -> "ur.jalandhry"
            "fr" -> "fr.hamidullah"
            "id" -> "id.indonesian"
            "tr" -> "tr.yazir"
            "am" -> "am.sadiq"
            "es" -> "es.cortes"
            "ru" -> "ru.kuliev"
            "bn" -> "bn.bengali"
            "hi" -> "hi.hindi"
            else -> "en.asad"
        }
    }


    private var ayahDao: com.example.data.local.AyahDao? = null
    private var appContext: android.content.Context? = null
    
    fun initialize(context: android.content.Context) {
        appContext = context.applicationContext
        ayahDao = com.example.data.local.AppDatabase.getDatabase(context).ayahDao()
    }

    /**
     * Parses the bundled offline mushaf exactly once (lazily) and keeps every
     * surah in memory. The file is ~2 MB of JSON, so re-parsing it per page —
     * or worse, on the main thread — was a major source of reader jank.
     */
    private val offlineAyahsBySurah: Map<Int, List<Ayah>> by lazy { parseOfflineMushafAsset() }

    private fun parseOfflineMushafAsset(): Map<Int, List<Ayah>> {
        val context = appContext ?: return emptyMap()
        return try {
            val jsonString = context.assets.open("quran_offline.json")
                .bufferedReader()
                .use { it.readText() }
            val jsonArray = org.json.JSONArray(jsonString)
            val result = HashMap<Int, List<Ayah>>(jsonArray.length())
            for (i in 0 until jsonArray.length()) {
                val surahObj = jsonArray.getJSONObject(i)
                val surahId = surahObj.getInt("id")
                val versesArray = surahObj.getJSONArray("verses")
                val ayahs = ArrayList<Ayah>(versesArray.length())
                for (j in 0 until versesArray.length()) {
                    val verseObj = versesArray.getJSONObject(j)
                    ayahs.add(
                        Ayah(
                            surahNumber = surahId,
                            ayahNumber = verseObj.getInt("verse_id"),
                            textArabic = verseObj.optString("ar", ""),
                            tafsir = null,
                            translations = mapOf("en" to verseObj.optString("en", ""))
                        )
                    )
                }
                result[surahId] = ayahs
            }
            result
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    private fun loadAyahsFromAssets(surahId: Int): List<Ayah> =
        offlineAyahsBySurah[surahId].orEmpty()

    /**
     * Loads the ayahs of one surah: memory cache → Room → network → bundled asset.
     *
     * Always executed on [Dispatchers.IO]: Room queries, network calls and the
     * asset JSON parsing must never block the frame rendering the UI.
     */
    suspend fun fetchAyahsForSurah(surahId: Int, langId: String = "en"): List<Ayah> =
        withContext(Dispatchers.IO) {
            val cacheKey = "$surahId-$langId"
            allAyahsCache[cacheKey]?.let { return@withContext it }

            // 1. Check Room Database
            val dao = ayahDao
            if (dao != null) {
                try {
                    val roomEntities = dao.getAyahsForSurah(surahId)
                    if (roomEntities.isNotEmpty()) {
                        val ayahsFromRoom = roomEntities.map { entity ->
                            Ayah(
                                surahNumber = entity.surahNumber,
                                ayahNumber = entity.ayahNumber,
                                textArabic = entity.textArabic,
                                tafsir = entity.tafsir,
                                translations = entity.translations
                            )
                        }
                        // Check if requested language is available in local entity
                        if (langId == "en" || ayahsFromRoom.any { it.translations.containsKey(langId) }) {
                            allAyahsCache[cacheKey] = ayahsFromRoom
                            return@withContext ayahsFromRoom
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 2. Try the online API
            try {
                val arabicResponse = com.example.data.api.RetrofitClient.apiService.getSurahArabic(surahId)
                val endpoint = getTranslationEndpoint(langId)
                val englishResponse = com.example.data.api.RetrofitClient.apiService.getSurahTranslation(surahId, endpoint)

                val ayahs = arabicResponse.data.ayahs.mapIndexed { index, arabicAyah ->
                    val englishAyah = englishResponse.data.ayahs.getOrNull(index)
                    val translations = if (englishAyah != null) {
                        mapOf(langId to englishAyah.text, "en" to englishAyah.text)
                    } else {
                        emptyMap()
                    }

                    Ayah(
                        surahNumber = surahId,
                        ayahNumber = arabicAyah.numberInSurah,
                        textArabic = arabicAyah.text,
                        tafsir = null,
                        translations = translations
                    )
                }

                if (dao != null && ayahs.isNotEmpty()) {
                    dao.insertAyahs(ayahs.toEntities())
                }

                allAyahsCache[cacheKey] = ayahs
                ayahs
            } catch (e: Exception) {
                // 3. API unavailable / device offline — fall back to the asset.
                val assetAyahs = loadAyahsFromAssets(surahId)
                if (assetAyahs.isNotEmpty()) {
                    if (dao != null) {
                        try {
                            dao.insertAyahs(assetAyahs.toEntities())
                        } catch (err: Exception) {
                            err.printStackTrace()
                        }
                    }
                    allAyahsCache[cacheKey] = assetAyahs
                    assetAyahs
                } else {
                    emptyList()
                }
            }
        }

    private fun List<Ayah>.toEntities(): List<com.example.data.local.AyahEntity> = map { ayah ->
        com.example.data.local.AyahEntity(
            id = "${ayah.surahNumber}_${ayah.ayahNumber}",
            surahNumber = ayah.surahNumber,
            ayahNumber = ayah.ayahNumber,
            textArabic = ayah.textArabic,
            tafsir = ayah.tafsir,
            translations = ayah.translations
        )
    }

    fun getDailyVerseInfo(dayOffset: Int = 0): Pair<Int, Int> {
        val epochDay = (System.currentTimeMillis() / (1000 * 60 * 60 * 24)).toInt()
        val index = Math.floorMod(epochDay + dayOffset, dailyVerseList.size)
        return dailyVerseList[index]
    }

    val dailyVerseList: List<Pair<Int, Int>> = listOf(
        Pair(1, 1),   // Al-Fatiha 1:1
        Pair(2, 152), // Al-Baqarah 2:152 - Remember Me; I will remember you
        Pair(2, 186), // Al-Baqarah 2:186 - Indeed I am near
        Pair(2, 255), // Al-Baqarah 2:255 - Ayat Al-Kursi
        Pair(2, 286), // Al-Baqarah 2:286 - Allah does not burden a soul beyond that it can bear
        Pair(3, 139), // Ali 'Imran 3:139 - Do not weaken and do not grieve
        Pair(3, 159), // Ali 'Imran 3:159 - Rely upon Allah
        Pair(3, 200), // Ali 'Imran 3:200 - Persevere and endure
        Pair(6, 17),  // Al-An'am 6:17 - If Allah touches you with adversity
        Pair(8, 30),  // Al-Anfal 8:30 - Allah is the best of planners
        Pair(9, 51),  // At-Tawbah 9:51 - What Allah has decreed for us
        Pair(13, 28), // Ar-Ra'd 13:28 - By the remembrance of Allah hearts find rest
        Pair(14, 7),  // Ibrahim 14:7 - If you are grateful, I will surely increase you
        Pair(20, 114),// Taha 20:114 - My Lord, increase me in knowledge
        Pair(21, 87), // Al-Anbiya 21:87 - There is no deity except You
        Pair(24, 35), // An-Nur 24:35 - Allah is the Light of the heavens and the earth
        Pair(25, 63), // Al-Furqan 25:63 - Servants of the Most Merciful
        Pair(28, 77), // Al-Qasas 28:77 - Seek the home of the Hereafter
        Pair(39, 53), // Az-Zumar 39:53 - Do not despair of the mercy of Allah
        Pair(40, 60), // Ghafir 40:60 - Call upon Me; I will respond to you
        Pair(55, 13), // Ar-Rahman 55:13 - Which of the favors of your Lord would you deny?
        Pair(65, 2),  // At-Talaq 65:2 - Whoever fears Allah - He will make a way out
        Pair(65, 3),  // At-Talaq 65:3 - And will provide for him from where he does not expect
        Pair(67, 1),  // Al-Mulk 67:1 - Blessed is He in whose hand is dominion
        Pair(93, 5),  // Ad-Duha 93:5 - Your Lord is going to give you, and you will be satisfied
        Pair(94, 5),  // Ash-Sharh 94:5 - For indeed, with hardship [will be] ease
        Pair(94, 6),  // Ash-Sharh 94:6 - Indeed, with hardship [will be] ease
        Pair(103, 1), // Al-'Asr 103:1 - By time, indeed mankind is in loss
        Pair(112, 1), // Al-Ikhlas 112:1 - Say, 'He is Allah, [who is] One'
        Pair(113, 1), // Al-Falaq 113:1 - Say, 'I seek refuge in the Lord of daybreak'
        Pair(114, 1)  // An-Nas 114:1 - Say, 'I seek refuge in the Lord of mankind'
    )
}