package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.TranslationLanguage
import com.example.data.repository.QuranData
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.GoldAccentLight
import com.example.util.QuranUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// -----------------------------------------------------------------------------
// Models
// -----------------------------------------------------------------------------

data class DuaItem(
    val id: String,
    val title: String,
    val arabicText: String,
    val transliteration: String,
    val translations: Map<String, String>,
    val reference: String
)

data class QuoteItem(
    val id: String,
    val author: String,
    val arabicText: String? = null,
    val translations: Map<String, String>,
    val category: String
)

data class DailyAyahInspiration(
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahNameEnglish: String,
    val surahNameArabic: String,
    val textArabic: String,
    val translatedText: String
)

data class InspirationUiState(
    val selectedLanguage: TranslationLanguage = TranslationLanguage.ENGLISH,
    val isLoading: Boolean = false,
    val dailyAyah: DailyAyahInspiration? = null,
    val dayOffset: Int = 0,
    val activeCategory: String = "All", // "All", "Daily Ayah", "Duas", "Wisdom"
    val bookmarkedIds: Set<String> = emptySet()
)

// -----------------------------------------------------------------------------
// Data Repositories for Duas & Quotes with Multi-lingual Support
// -----------------------------------------------------------------------------

object InspirationRepository {

    val sampleDuas: List<DuaItem> = listOf(
        DuaItem(
            id = "dua_1",
            title = "Dua for Knowledge & Wisdom",
            arabicText = "رَّبِّ زِدْنِي عِلْمًا",
            transliteration = "Rabbi zidni 'ilma",
            translations = mapOf(
                "en" to "O my Lord! Increase me in knowledge. (Surah Taha 20:114)",
                "ur" to "اے میرے رب! میرے علم میں اضافہ فرما۔",
                "id" to "Ya Tuhanku, tambahkanlah kepadaku ilmu pengetahuan.",
                "fr" to "O mon Seigneur, augmente mes connaissances.",
                "es" to "¡Oh Señor mío! Auméntame el conocimiento.",
                "tr" to "Rabbim! İlmimi artır.",
                "bn" to "হে আমার পালনকর্তা, আমার জ্ঞান বৃদ্ধি করুন।",
                "hi" to "ऐ मेरे रब! मेरे ज्ञान में वृद्धि कर।",
                "ru" to "Господи мой! Приумножь мои знания.",
                "de" to "O mein Herr, mehre mein Wissen.",
                "am" to "አቤቱ ጌታዬ ሆይ! እውቀትን ጨምርልኝ።"
            ),
            reference = "Surah Taha (20:114)"
        ),
        DuaItem(
            id = "dua_2",
            title = "Dua for Patience & Perseverance",
            arabicText = "رَبَّنَا أَفْرِغْ عَلَيْنَا صَبْرًا وَثَبِّتْ أَقْدَامَنَا وَٱنصُرْنَا عَلَى ٱلْقَوْمِ ٱلْكَٰفِرِينَ",
            transliteration = "Rabbana afrig 'alayna sabran wa thabbit aqdamana wansurna 'alal-qawmil-kafirin",
            translations = mapOf(
                "en" to "Our Lord, pour upon us patience and plant firmly our feet and give us victory over the disbelieving people.",
                "ur" to "اے ہمارے رب! ہم پر صبر کے دہانے کھول دے اور ہمارے قدم جما دے اور کافروں کی قوم پر ہماری مدد فرما۔",
                "id" to "Ya Tuhan kami, limpahkanlah kesabaran kepada kami, kukuhkanlah langkah kami dan tolonglah kami menghadapi orang-orang kafir.",
                "fr" to "Notre Seigneur! Déverse sur nous la patience, affermis nos pas et donne-nous la victoire sur le peuple infidèle.",
                "es" to "¡Señor nuestro! Infunde en nosotros paciencia, afirma nuestros pasos y concédenos la victoria sobre el pueblo infiel.",
                "tr" to "Rabbimiz! Üzerimize sabır yağdır, ayaklarımızı sağlam bastır ve kâfir kavme karşı bize yardım et.",
                "bn" to "হে আমাদের পালনকর্তা! আমাদের ওপর ধৈর্য বর্ষণ করুন, আমাদের কদম অবিচল রাখুন এবং কাফের সম্প্রদায়ের বিরুদ্ধে আমাদের সাহায্য করুন।",
                "hi" to "ऐ ہمارے रब! हम पर सब्र का उंडेलना कर और हमारे क़दम जमा दे और काफ़िरों की क़ौम के मुक़ाबले में हमारी मदद कर।",
                "ru" to "Господь наш! Пролей на нас терпение, укрепи наши стопы и помоги нам против людей неверующих.",
                "de" to "Unser Herr, gieße Geduld über uns aus, festige unsere Füße und hilf uns gegen das ungläubige Volk.",
                "am" to "ጌታችን ሆይ! በእኛ ላይ ትዕግስትን አፍስስ፤ እግሮቻችንንም አጽና፤ በከሃዲዎችም ሕዝቦች ላይ እርዳን።"
            ),
            reference = "Surah Al-Baqarah (2:250)"
        ),
        DuaItem(
            id = "dua_3",
            title = "Dua for Forgiveness & Mercy",
            arabicText = "رَبَّنَا ظَلَمْنَا أَنفُسَنَا وَإِن لَّمْ تَغْفِرْ لَنَا وَتَرْحَمْنَا لَنَكُونَنَّ مِنَ ٱلْخَٰسِرِينَ",
            transliteration = "Rabbana zalamna anfusana wa il-lam taghfir lana wa tarhamna lanakunanna minal-khasirin",
            translations = mapOf(
                "en" to "Our Lord, we have wronged ourselves, and if You do not forgive us and have mercy upon us, we will surely be among the losers.",
                "ur" to "اے ہمارے رب! ہم نے اپنی جانوں پر ظلم کیا اور اگر تو نے ہمیں معاف نہ فرمایا اور ہم پر رحم نہ کیا تو ہم یقیناً نقصان اٹھانے والوں میں سے ہو جائیں گے۔",
                "id" to "Ya Tuhan kami, kami telah menzalimi diri kami sendiri. Jika Engkau tidak mengampuni kami dan memberi rahmat kepada kami, niscaya kami termasuk orang-orang yang rugi.",
                "fr" to "O notre Seigneur, nous avons fait du tort à nous-mêmes. Et si Tu ne nous pardonnes pas et ne nous fais pas miséricorde, nous serons très certainement du nombre des perdants.",
                "es" to "Señor nuestro, hemos sido injustos con nosotros mismos; si no nos perdonas y te apiadas de nosotros, seremos de los perdedores.",
                "tr" to "Rabbimiz! Biz kendimize zulmettik. Eğer bizi bağışlamaz ve bize merhamet etmezsen, mutlaka ziyana uğrayanlardan oluruz.",
                "bn" to "হে আমাদের পালনকর্তা! আমরা নিজেদের প্রতি জুলুম করেছি। আপনি যদি আমাদের ক্ষমা না করেন এবং আমাদের প্রতি দয়া না করেন, তবে অবশ্যই আমরা ক্ষতিগ্রস্তদের অন্তর্ভুক্ত হব।",
                "hi" to "ऐ मेरे रब! हमने अपने आप पर ज़ुल्म किया, और यदि तूने हमें न बख़्शा और हम पर रहम न किया, तो यक़ीनन हम घाटा उठाने वालों में से हो जाएँगे।",
                "ru" to "Господь наш! Мы поступили несправедливо по отношению к себе, и если Ты не простишь нас и не помилуешь, то мы окажемся в числе потерпевших убыток.",
                "de" to "Unser Herr, wir haben uns selbst Unrecht angetan. Wenn Du uns nicht vergibst und Dich unser erbarmst, werden wir ganz gewiss zu den Verlorenen gehören.",
                "am" to "ጌታችን ሆይ! ነፍሶቻችንን በደልን፤ ለእኛ ባትምርልንና ባትራራልን በእርግጥ ከከሳሪዎቹ እንሆናለን።"
            ),
            reference = "Surah Al-A'raf (7:23)"
        ),
        DuaItem(
            id = "dua_4",
            title = "Dua for Ease in Difficulties",
            arabicText = "رَبِّ ٱشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي وَٱحْلُلْ عُقْدَةً مِّن لِّسَانِي يَفْقَهُوا۟ قَوْلِي",
            transliteration = "Rabbi-shrah li sadri, wa yassir li amri, wahlul 'uqdatam mil-lisani yafqahu qawli",
            translations = mapOf(
                "en" to "O my Lord! Expand for me my breast, and ease for me my task, and untie the knot from my tongue that they may understand my speech.",
                "ur" to "اے میرے رب! میرا سینہ کھول دے اور میرے لیے میرا کام آسان کر دے اور میری زبان کی گرہ کھول دے تاکہ وہ میری بات سمجھ سکیں۔",
                "id" to "Ya Tuhanku, lapangkanlah untukku dadaku, dan mudahkanlah untukku urusanku, dan lepaskanlah kekakuan dari lidahku, supaya mereka mengerti perkataanku.",
                "fr" to "Seigneur, ouvre-moi ma poitrine, et facilite-moi ma tâche, et délie un nœud de ma langue, afin qu'ils comprennent mes paroles.",
                "es" to "¡Señor mío! Ensancha mi pecho, facilítame la tarea y desata el nudo de mi lengua para que comprendan mi discurso.",
                "tr" to "Rabbim! Göğsümü genişlet, işimi kolaylaştır, dilimdeki düğümü çöz ki sözümü anlasınlar.",
                "bn" to "হে আমার পালনকর্তা! আমার বক্ষ প্রশস্ত করে দিন, আমার কাজ সহজ করে দিন এবং আমার জিভের আড়ষ্টতা দূর করে দিন, যাতে তারা আমার কথা বুঝতে পারে।",
                "hi" to "ऐ मेरे रब! मेरा सीना खोल दे, और मेरे लिए मेरा काम आसान कर दे, और मेरी ज़बान की गिरह खोल दे ताकि वे मेरी बात समझ सकें।",
                "ru" to "Господи! Раскрой для меня мою грудь, облегчи мою миссию и развяжи узел на моем языке, чтобы они могли понять мою речь.",
                "de" to "Mein Herr, weite mir meine Brust, und mache mir meine Aufgabe leicht, und löse den Knoten von meiner Zunge, dass sie meine Rede verstehen.",
                "am" to "ጌታዬ ሆይ! ደረቴን አስፋልኝ፤ ነገሬንም አገራልኝ፤ ከአንደበቴም እሰሩን ፍታልኝ፤ ንግግሬን ይረዱ ዘንድ።"
            ),
            reference = "Surah Taha (20:25-28)"
        )
    )

    val sampleQuotes: List<QuoteItem> = listOf(
        QuoteItem(
            id = "quote_1",
            author = "Quran (94:5-6)",
            arabicText = "فَإِنَّ مَعَ ٱلْعُسْرِ يُسْرًا • إِنَّ مَعَ ٱلْعُسْرِ يُسْرًا",
            translations = mapOf(
                "en" to "For indeed, with hardship comes ease. Indeed, with hardship comes ease.",
                "ur" to "پس یقیناً مشکل کے ساتھ آسانی ہے، بے شک مشکل کے ساتھ آسانی ہے۔",
                "id" to "Maka sesungguhnya bersama kesulitan ada kemudahan. Sesungguhnya bersama kesulitan ada kemudahan.",
                "fr" to "Car à côté de la difficulté il y a certes une facilité! Oui, à côté de la difficulté il y a une facilité!",
                "es" to "Porque, ciertamente, tras la dificultad viene la facilidad. Ciertamente, tras la dificultad viene la facilidad.",
                "tr" to "Demek ki zorlukla beraber bir kolaylık var. Evet, zorlukla beraber bir kolaylık var.",
                "bn" to "সুতরাং কষ্টের সাথেই রয়েছে স্বস্তি। নিশ্চয়ই কষ্টের সাথেই রয়েছে স্বস্তি।",
                "hi" to "तो यक़ीनन कठिनाई के साथ आसानी है, निस्संदेह कठिनाई के साथ आसानी है।",
                "ru" to "Воистину, за каждой тяжестью наступает облегчение. Воистину, за каждой тяжестью наступает облегчение.",
                "de" to "Denn gewiss, mit der Erschwernis ist Erleichterung. Gewiss, mit der Erschwernis ist Erleichterung.",
                "am" to "ከችግር ጋር በእርግጥ ማገገም አለና፤ ከችግር ጋር በእርግጥ ማገገም አለና።"
            ),
            category = "Patience & Hope"
        ),
        QuoteItem(
            id = "quote_2",
            author = "Quran (3:159)",
            arabicText = "فَإِذَا عَزَمْتَ فَتَوَكَّلْ عَلَى ٱللَّهِ ۚ إِنَّ ٱللَّهَ يُحِبُّ ٱلْمُتَوَكِّلِينَ",
            translations = mapOf(
                "en" to "When you have taken a decision, put your trust in Allah. Indeed, Allah loves those who trust in Him.",
                "ur" to "پھر جب تم عزم کر لو تو اللہ پر بھروسہ کرو، بے شک اللہ بھروسہ کرنے والوں سے محبت کرتا ہے۔",
                "id" to "Kemudian, apabila engkau telah membulatkan tekad, maka bertawakallah kepada Allah. Sungguh, Allah mencintai orang-orang yang bertawakal.",
                "fr" to "Puis, une fois que tu as pris une décision, confie-toi à Allah. Car Allah aime ceux qui Lui font confiance.",
                "es" to "Y cuando hayas tomado una decisión, confía en Allah. Ciertamente Allah ama a los que confían en Él.",
                "tr" to "Kararını verdiğin zaman artık Allah'a dayanıp güven. Şüphesiz Allah, Kendisine dayanıp güvenenleri sever.",
                "bn" to "অতঃপর যখন আপনি সিদ্ধান্ত গ্রহণ করবেন, তখন আল্লাহর ওপর ভরসা করুন। নিশ্চয়ই আল্লাহ ভরসাকারীদের ভালোবাসেন।",
                "hi" to "फिर जब तुम कोई इरादा कर लो तो अल्लाह पर भरोसा रखो। बेशक अल्लाह भरोसा रखने वालों से मोहब्बत करता है।",
                "ru" to "Когда же ты примешь решение, то уповай на Аллаха, ведь Аллах любит уповающих.",
                "de" to "Wenn du dich entschieden hast, dann vertraue auf Allah. Gewiss, Allah liebt die Vertrauenden.",
                "am" to "ቁርጥ ውሳኔ ባደረግክም ጊዜ በአላህ ላይ ተመካ፤ አላህ ተመኪዎችን ይወዳልና።"
            ),
            category = "Trust in Allah"
        ),
        QuoteItem(
            id = "quote_3",
            author = "Quran (13:28)",
            arabicText = "أَلَا بِذِكْرِ ٱللَّهِ تَطْمَئِنُّ ٱلْقُلُوبُ",
            translations = mapOf(
                "en" to "Unquestionably, by the remembrance of Allah do hearts find rest.",
                "ur" to "خبردار! اللہ کے ذکر ہی سے دلوں کو اطمینان حاصل ہوتا ہے۔",
                "id" to "Ingatlah, hanya dengan mengingat Allah hati menjadi tenteram.",
                "fr" to "N'est-ce pas par l'évocation d'Allah que se tranquillisent les cœurs?",
                "es" to "Ciertamente, en el recuerdo de Allah encuentran paz los corazones.",
                "tr" to "Bilesiniz ki, kalpler ancak Allah'ı anmakla huzur bulur.",
                "bn" to "জেনে রাখো, আল্লাহর জিকির দ্বারাই অন্তরসমূহ প্রশান্ত হয়।",
                "hi" to "सुन लो! अल्लाह की याद से ही दिलों को इत्मीनान (शांति) मिलता है।",
                "ru" to "Разве не поминанием Аллаха утешаются сердца?",
                "de" to "Gedenken nicht die Herzen im Gedenken Allahs der Ruhe?",
                "am" to "ንቁ! አላህን በማውሳት ልቦች ይረጋጋሉ።"
            ),
            category = "Inner Peace"
        ),
        QuoteItem(
            id = "quote_4",
            author = "Prophet Muhammad (ﷺ)",
            arabicText = "إِنَّمَا بُعِثْتُ لِأُتَمِّمَ صَالِحَ الْأَخْلَاقِ",
            translations = mapOf(
                "en" to "I was sent only to perfect noble character.",
                "ur" to "مجھے بہترین اخلاق کی تکمیل کے لیے بھیجا گیا ہے۔",
                "id" to "Sesungguhnya aku diutus hanya untuk menyempurnakan akhlak yang mulia.",
                "fr" to "Je n'ai été envoyé que pour parfaire les nobles comportements.",
                "es" to "Ciertamente he sido enviado solo para perfeccionar el buen carácter.",
                "tr" to "Ben ancak güzel ahlakı tamamlamak için gönderildim.",
                "bn" to "আমাকে কেবল উত্তম চরিত্রকে পূর্ণতা দান করার জন্যই প্রেরণ করা হয়েছে।",
                "hi" to "मुझे केवल उत्तम चरित्र को पूर्ण करने के लिए भेजा गया है।",
                "ru" to "Я был послан лишь для того, чтобы довести благородный нрав до совершенства.",
                "de" to "Ich wurde nur gesandt, um die edlen Charaktereigenschaften zu vollenden.",
                "am" to "እኔ የተላክሁት የመልካም ሥነ-ምግባርን ማጠናቀቅ ብቻ ነው።"
            ),
            category = "Noble Character"
        )
    )
}

// -----------------------------------------------------------------------------
// ViewModel Logic
// -----------------------------------------------------------------------------

class InspirationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(InspirationUiState())
    val uiState: StateFlow<InspirationUiState> = _uiState.asStateFlow()

    init {
        loadInspirationContent()
    }

    /**
     * CRITICAL REQUIREMENT 1 & 3:
     * Sets the translation language using the shared `TranslationLanguage` enum
     * and dynamically refreshes all inspiration content (Daily Ayah, Duas, Quotes).
     */
    fun selectLanguage(language: TranslationLanguage) {
        if (_uiState.value.selectedLanguage == language) return
        _uiState.update { it.copy(selectedLanguage = language) }
        loadInspirationContent()
    }

    fun setCategory(category: String) {
        _uiState.update { it.copy(activeCategory = category) }
    }

    fun nextDailyVerse() {
        _uiState.update { it.copy(dayOffset = it.dayOffset + 1) }
        loadInspirationContent()
    }

    fun toggleBookmark(id: String) {
        _uiState.update { current ->
            val updated = current.bookmarkedIds.toMutableSet()
            if (updated.contains(id)) updated.remove(id) else updated.add(id)
            current.copy(bookmarkedIds = updated)
        }
    }

    private fun loadInspirationContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val currentLang = _uiState.value.selectedLanguage
            val (surahId, ayahNum) = QuranData.getDailyVerseInfo(_uiState.value.dayOffset)

            val ayahs = QuranData.fetchAyahsForSurah(surahId, currentLang.id)
            val ayah = ayahs.find { it.ayahNumber == ayahNum }
                ?: ayahs.firstOrNull()
                ?: com.example.data.model.Ayah(
                    surahNumber = surahId,
                    ayahNumber = ayahNum,
                    textArabic = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                    translations = mapOf("en" to "In the name of Allah, the Entirely Merciful, the Especially Merciful.")
                )

            val cleanArabic = QuranUtils.formatAyahText(ayah.surahNumber, ayah.ayahNumber, ayah.textArabic)
            val surahInfo = QuranData.surahById(surahId)

            val translatedText = ayah.translations[currentLang.id]
                ?: ayah.translations["en"]
                ?: "Translation unavailable for selected language."

            val dailyAyahInspiration = DailyAyahInspiration(
                surahNumber = surahId,
                ayahNumber = ayahNum,
                surahNameEnglish = surahInfo?.nameEnglish ?: "Surah $surahId",
                surahNameArabic = surahInfo?.nameArabic ?: "سورة",
                textArabic = cleanArabic,
                translatedText = translatedText
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    dailyAyah = dailyAyahInspiration
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// InspirationScreen Composable
// -----------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InspirationScreen(
    viewModel: InspirationViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onPlayAudio: (Int, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header & Modern Shared Language Selector
        InspirationHeaderWithLanguageSelector(
            selectedLanguage = uiState.selectedLanguage,
            onLanguageSelected = { viewModel.selectLanguage(it) }
        )

        // Category Navigation Chips
        InspirationCategoryTabs(
            selectedCategory = uiState.activeCategory,
            onCategorySelected = { viewModel.setCategory(it) }
        )

        // Main Content List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Daily Ayah Section
            if (uiState.activeCategory == "All" || uiState.activeCategory == "Daily Ayah") {
                item {
                    val dailyAyah = uiState.dailyAyah
                    if (dailyAyah != null) {
                        DailyAyahCard(
                            dailyAyah = dailyAyah,
                            selectedLanguage = uiState.selectedLanguage,
                            isBookmarked = uiState.bookmarkedIds.contains("ayah_${dailyAyah.surahNumber}_${dailyAyah.ayahNumber}"),
                            onNextVerse = { viewModel.nextDailyVerse() },
                            onPlayAudio = { onPlayAudio(dailyAyah.surahNumber, dailyAyah.ayahNumber) },
                            onBookmark = { viewModel.toggleBookmark("ayah_${dailyAyah.surahNumber}_${dailyAyah.ayahNumber}") },
                            onCopy = {
                                copyToClipboard(
                                    context,
                                    "${dailyAyah.textArabic}\n\n\"${dailyAyah.translatedText}\"\n— Surah ${dailyAyah.surahNameEnglish} (${dailyAyah.surahNumber}:${dailyAyah.ayahNumber})"
                                )
                            }
                        )
                    }
                }
            }

            // Duas & Supplications Section
            if (uiState.activeCategory == "All" || uiState.activeCategory == "Duas") {
                item {
                    SectionHeaderTitle(
                        title = "Supplications & Duas",
                        subtitle = "Quranic prayers for guidance and peace",
                        icon = Icons.Default.Favorite
                    )
                }

                items(InspirationRepository.sampleDuas, key = { it.id }) { dua ->
                    DuaCard(
                        dua = dua,
                        selectedLanguage = uiState.selectedLanguage,
                        isBookmarked = uiState.bookmarkedIds.contains(dua.id),
                        onBookmark = { viewModel.toggleBookmark(dua.id) },
                        onCopy = {
                            val translated = dua.translations[uiState.selectedLanguage.id] ?: dua.translations["en"] ?: ""
                            copyToClipboard(context, "${dua.arabicText}\n\n\"$translated\"\n— ${dua.reference}")
                        }
                    )
                }
            }

            // Islamic Wisdom & Quotes Section
            if (uiState.activeCategory == "All" || uiState.activeCategory == "Wisdom") {
                item {
                    SectionHeaderTitle(
                        title = "Islamic Wisdom & Reflections",
                        subtitle = "Sayings and verses to enlighten the soul",
                        icon = Icons.Default.FormatQuote
                    )
                }

                items(InspirationRepository.sampleQuotes, key = { it.id }) { quote ->
                    QuoteCard(
                        quote = quote,
                        selectedLanguage = uiState.selectedLanguage,
                        isBookmarked = uiState.bookmarkedIds.contains(quote.id),
                        onBookmark = { viewModel.toggleBookmark(quote.id) },
                        onCopy = {
                            val translated = quote.translations[uiState.selectedLanguage.id] ?: quote.translations["en"] ?: ""
                            copyToClipboard(context, "\"$translated\"\n— ${quote.author}")
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// -----------------------------------------------------------------------------
// Component: Top Language Selector Header
// -----------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InspirationHeaderWithLanguageSelector(
    selectedLanguage: TranslationLanguage,
    onLanguageSelected: (TranslationLanguage) -> Unit
) {
    var expandedDropdown by remember { mutableStateOf(false) }

    Surface(
        color = Color(0xFF141C18),
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = GoldAccentLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Daily Inspiration",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Reflections, Verses & Supplications",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray.copy(alpha = 0.7f)
                    )
                }

                // Modern Pill Dropdown for Shared Translation Languages
                Box {
                    Surface(
                        onClick = { expandedDropdown = true },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                        modifier = Modifier.testTag("inspiration_language_selector")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedLanguage.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // CRITICAL REQUIREMENT 1: Use exact same shared `TranslationLanguage` enum entries
                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false },
                        modifier = Modifier
                            .background(Color(0xFF1F2823))
                            .border(1.dp, Color(0xFF2E3D35), RoundedCornerShape(12.dp))
                    ) {
                        TranslationLanguage.values().forEach { language ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = language.displayName,
                                            fontWeight = if (language == selectedLanguage) FontWeight.Bold else FontWeight.Normal,
                                            color = if (language == selectedLanguage) GoldAccentLight else Color.White
                                        )
                                        if (language == selectedLanguage) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = GoldAccentLight,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onLanguageSelected(language)
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // CRITICAL REQUIREMENT 2: Horizontal Quick Pill Selector for shared languages
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = TranslationLanguage.entries,
                    key = { lang -> lang.id }
                ) { lang ->
                    val isSelected = lang == selectedLanguage
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF222D28),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) GoldAccentLight else Color(0xFF2E3D35)
                        ),
                        modifier = Modifier.clickable { onLanguageSelected(lang) }
                    ) {
                        Text(
                            text = lang.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color.LightGray,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Component: Category Navigation Chips
// -----------------------------------------------------------------------------

@Composable
private fun InspirationCategoryTabs(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf("All", "Daily Ayah", "Duas", "Wisdom")

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = categories,
            key = { category -> category }
        ) { category ->
            val isSelected = category == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// Component: Daily Ayah Card
// -----------------------------------------------------------------------------

@Composable
private fun DailyAyahCard(
    dailyAyah: DailyAyahInspiration,
    selectedLanguage: TranslationLanguage,
    isBookmarked: Boolean,
    onNextVerse: () -> Unit,
    onPlayAudio: () -> Unit,
    onBookmark: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inspiration_daily_ayah_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF16241D)
        ),
        border = BorderStroke(1.dp, GoldAccentLight.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E3328),
                            Color(0xFF121E18)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Badge & Action Icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GoldAccentLight.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, GoldAccentLight)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = GoldAccentLight,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ayah of the Day",
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldAccentLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = onNextVerse) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Next Verse",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onPlayAudio) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Play Verse Audio",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onBookmark) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark Verse",
                                tint = if (isBookmarked) GoldAccentLight else Color.LightGray
                            )
                        }
                        IconButton(onClick = onCopy) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Verse",
                                tint = Color.LightGray
                            )
                        }
                    }
                }

                // Arabic Ayah Text
                Text(
                    text = dailyAyah.textArabic,
                    fontFamily = AmiriFontFamily,
                    fontSize = 24.sp,
                    lineHeight = 42.sp,
                    color = Color.White,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                // Translated Text dynamically updated for `selectedLanguage`
                Text(
                    text = dailyAyah.translatedText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFE2E8E5),
                    lineHeight = 24.sp
                )

                // Surah Badge & Language Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Surah ${dailyAyah.surahNameEnglish} (${dailyAyah.surahNameArabic}) • ${dailyAyah.surahNumber}:${dailyAyah.ayahNumber}",
                        style = MaterialTheme.typography.labelLarge,
                        color = GoldAccentLight,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF283A30)
                    ) {
                        Text(
                            text = selectedLanguage.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Component: Dua Card
// -----------------------------------------------------------------------------

@Composable
private fun DuaCard(
    dua: DuaItem,
    selectedLanguage: TranslationLanguage,
    isBookmarked: Boolean,
    onBookmark: () -> Unit,
    onCopy: () -> Unit
) {
    val translatedText = dua.translations[selectedLanguage.id]
        ?: dua.translations["en"]
        ?: "Translation unavailable."

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inspiration_dua_card_${dua.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dua.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row {
                    IconButton(onClick = onBookmark) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark Dua",
                            tint = if (isBookmarked) GoldAccentLight else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onCopy) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Dua",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Arabic
            Text(
                text = dua.arabicText,
                fontFamily = AmiriFontFamily,
                fontSize = 22.sp,
                lineHeight = 38.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            // Transliteration
            Text(
                text = dua.transliteration,
                style = MaterialTheme.typography.bodyMedium,
                color = GoldAccentLight,
                fontWeight = FontWeight.Medium
            )

            // Localized Translation dynamically updated
            Text(
                text = translatedText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Reference Footnote
            Text(
                text = dua.reference,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

// -----------------------------------------------------------------------------
// Component: Quote Card
// -----------------------------------------------------------------------------

@Composable
private fun QuoteCard(
    quote: QuoteItem,
    selectedLanguage: TranslationLanguage,
    isBookmarked: Boolean,
    onBookmark: () -> Unit,
    onCopy: () -> Unit
) {
    val translatedQuote = quote.translations[selectedLanguage.id]
        ?: quote.translations["en"]
        ?: "Quote unavailable."

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inspiration_quote_card_${quote.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E2622)
        ),
        border = BorderStroke(0.5.dp, Color(0xFF2E3D35))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = quote.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row {
                    IconButton(onClick = onBookmark) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark Quote",
                            tint = if (isBookmarked) GoldAccentLight else Color.LightGray
                        )
                    }
                    IconButton(onClick = onCopy) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Quote",
                            tint = Color.LightGray
                        )
                    }
                }
            }

            if (quote.arabicText != null) {
                Text(
                    text = quote.arabicText,
                    fontFamily = AmiriFontFamily,
                    fontSize = 20.sp,
                    color = Color.White,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Localized Quote Translation
            Text(
                text = "\"$translatedQuote\"",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE0E0E0),
                lineHeight = 24.sp
            )

            // Author
            Text(
                text = "— ${quote.author}",
                style = MaterialTheme.typography.labelLarge,
                color = GoldAccentLight,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// -----------------------------------------------------------------------------
// Component: Section Header Title
// -----------------------------------------------------------------------------

@Composable
private fun SectionHeaderTitle(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

// Helper Toast Clipboard function
private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Inspiration", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
}
