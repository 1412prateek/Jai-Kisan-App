package com.rcc.jai_kisan;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Production-grade Centralized Localization & Dynamic Multilingual Manager.
 * Supports dual-language format "English / (Regional Language)" dynamically across the entire app and PDFs,
 * or "English Only" when chosen.
 */
public class LanguageManager {

    private static final String PREFS_NAME = "jai_kisan_lang_prefs";
    private static final String KEY_SELECTED_LANG = "selected_language";
    public static final String DEFAULT_LANG = "hi"; // Default Hindi

    public static class LangItem {
        public final String code;
        public final String displayName; // e.g. "Hindi / हिंदी"
        public final String englishName;
        public final String nativeName;

        public LangItem(String code, String displayName, String englishName, String nativeName) {
            this.code = code;
            this.displayName = displayName;
            this.englishName = englishName;
            this.nativeName = nativeName;
        }
    }

    public static final LangItem[] SUPPORTED_LANGUAGES = {
            new LangItem("hi", "Hindi / हिंदी", "Hindi", "हिंदी"),
            new LangItem("bn", "Bengali / বাংলা", "Bengali", "বাংলা"),
            new LangItem("te", "Telugu / తెలుగు", "Telugu", "తెలుగు"),
            new LangItem("mr", "Marathi / मराठी", "Marathi", "मराठी"),
            new LangItem("ta", "Tamil / தமிழ்", "Tamil", "தமிழ்"),
            new LangItem("gu", "Gujarati / ગુજરાતી", "Gujarati", "ગુજરાતી"),
            new LangItem("kn", "Kannada / ಕನ್ನಡ", "Kannada", "ಕನ್ನಡ"),
            new LangItem("ml", "Malayalam / മലയാളം", "Malayalam", "മലയാളം"),
            new LangItem("pa", "Punjabi / ਪੰਜਾਬੀ", "Punjabi", "ਪੰਜਾਬੀ"),
            new LangItem("en", "English Only", "English", "English")
    };

    public static String[] getLanguageDisplayNames() {
        String[] names = new String[SUPPORTED_LANGUAGES.length];
        for (int i = 0; i < SUPPORTED_LANGUAGES.length; i++) {
            names[i] = SUPPORTED_LANGUAGES[i].displayName;
        }
        return names;
    }

    public static String getLanguage(Context context) {
        if (context == null) return DEFAULT_LANG;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_SELECTED_LANG, DEFAULT_LANG);
    }

    public static int getLanguageIndex(Context context) {
        String code = getLanguage(context);
        for (int i = 0; i < SUPPORTED_LANGUAGES.length; i++) {
            if (SUPPORTED_LANGUAGES[i].code.equalsIgnoreCase(code)) {
                return i;
            }
        }
        return 0;
    }

    public static void setLanguage(Context context, String langCode) {
        if (context == null || langCode == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_SELECTED_LANG, langCode).apply();
    }

    public static String getActiveLanguageDisplayName(Context context) {
        int idx = getLanguageIndex(context);
        return SUPPORTED_LANGUAGES[idx].displayName;
    }

    public static boolean isEnglishOnly(Context context) {
        return "en".equalsIgnoreCase(getLanguage(context));
    }

    public static Context wrapContext(Context context) {
        String langCode = getLanguage(context);
        Locale locale = new Locale(langCode.equalsIgnoreCase("en") ? "en" : langCode);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(new LocaleList(locale));
            return context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
            return context;
        }
    }

    // Comprehensive dictionary for all 10 languages
    private static final Map<String, Map<String, String>> DICTIONARY = new HashMap<>();

    static {
        // Hindi
        Map<String, String> hi = new HashMap<>();
        hi.put("jai_kisan", "जय किसान");
        hi.put("app_motto", "मेरी मिट्टी, मेरे किसान");
        hi.put("financial_pro_report", "वित्तीय प्रो रिपोर्ट");
        hi.put("crop", "फसल");
        hi.put("state", "राज्य");
        hi.put("total_cost", "कुल लागत");
        hi.put("cost_breakdown", "लागत का विवरण");
        hi.put("harvest_estimation", "पैदावार और राजस्व");
        hi.put("seed", "बीज");
        hi.put("fertilizer", "खाद");
        hi.put("labor", "मजदूर");
        hi.put("other", "अन्य");
        hi.put("total_yield", "कुल पैदावार");
        hi.put("price", "भाव");
        hi.put("total_revenue", "कुल राजस्व");
        hi.put("net_profit", "शुद्ध लाभ");
        hi.put("profit_margin", "लाभ प्रतिशत");
        hi.put("break_even", "लागत-वसूली दर");
        hi.put("good_crop_choice", "सही फसल चुनाव");
        hi.put("land_area", "जमीन");
        hi.put("recommended_fertilizer", "अनुशंसित उर्वरक");
        hi.put("shopping_list", "खरीद सूची");
        hi.put("liquid_option", "तरल विकल्प (Nano Urea)");
        hi.put("ai_advisory", "एआई विशेषज्ञ सलाह");
        hi.put("urea", "यूरिया");
        hi.put("dap", "डीएपी");
        hi.put("mop", "एमओपी");
        hi.put("nitrogen", "नाइट्रोजन");
        hi.put("phosphorus", "फॉस्फोरस");
        hi.put("potassium", "पोटेशियम");
        hi.put("soil_type", "मिट्टी का प्रकार");
        hi.put("irrigation_method", "सिंचाई विधि");
        hi.put("critical_stage", "महत्वपूर्ण चरण");
        hi.put("expert_tip", "विशेषज्ञ सलाह");
        hi.put("irrigation_report", "सिंचाई योजना रिपोर्ट");
        hi.put("yield_report", "पैदावार अनुमान रिपोर्ट");
        hi.put("mandi_report", "मंडी भाव रिपोर्ट");
        hi.put("diagnosis_report", "फसल स्वास्थ्य रिपोर्ट");
        hi.put("voice_report", "आवाज सहायक रिपोर्ट");
        hi.put("share_report", "रिपोर्ट साझा करें");
        hi.put("share_text", "टेक्स्ट के रूप में साझा करें");
        hi.put("share_pdf", "पीडीएफ के रूप में साझा करें");
        hi.put("cancel", "रद्द करें");
        hi.put("settings", "सेटिंग्स");
        hi.put("language", "भाषा");
        hi.put("photo_expert", "फोटो विशेषज्ञ");
        hi.put("capture", "फोटो लें");
        hi.put("upload", "अपलोड करें");
        hi.put("analyze_photo", "फोटो का विश्लेषण करें");
        hi.put("date", "दिनांक");
        hi.put("generated_via", "जय किसान ऐप द्वारा निर्मित • भारतीय कृषि का संबल");
        hi.put("home_detect_disease", "रोग की पहचान");
        hi.put("home_voice_assistant", "आवाज़ सहायक");
        hi.put("home_mandi_prices", "मंडी के भाव");
        hi.put("home_how_to_use", "कैसे इस्तेमाल करे");
        hi.put("home_weather_info", "मौसम जानकारी");
        hi.put("home_farming_tools", "खेती उपकरण");
        hi.put("home_about", "परिचय");
        hi.put("fertilizer_calculator", "उर्वरक कैलकुलेटर");
        hi.put("yield_estimator", "पैदावार अनुमान");
        hi.put("irrigation_planner", "सिंचाई योजना");
        hi.put("cost_calculator", "लागत कैलकुलेटर");
        DICTIONARY.put("hi", hi);

        // Bengali
        Map<String, String> bn = new HashMap<>();
        bn.put("jai_kisan", "জয় কিষাণ");
        bn.put("app_motto", "আমার মাটি, আমার কৃষক");
        bn.put("financial_pro_report", "আর্থিক প্রো রিপোর্ট");
        bn.put("crop", "ফসল");
        bn.put("state", "রাজ্য");
        bn.put("total_cost", "মোট খরচ");
        bn.put("cost_breakdown", "খরচের বিবরণ");
        bn.put("harvest_estimation", "ফলন ও রাজস্ব");
        bn.put("seed", "বীজ");
        bn.put("fertilizer", "সার");
        bn.put("labor", "শ্রমিক");
        bn.put("other", "অন্যান্য");
        bn.put("total_yield", "মোট ফলন");
        bn.put("price", "দর");
        bn.put("total_revenue", "মোট আয়");
        bn.put("net_profit", "নিট লাভ");
        bn.put("profit_margin", "মুনাফা মার্জিন");
        bn.put("break_even", "ব্রেক-ইভেন দর");
        bn.put("good_crop_choice", "উত্তম ফসল নির্বাচন");
        bn.put("land_area", "জমির পরিমাণ");
        bn.put("recommended_fertilizer", "সুপারিশকৃত সার");
        bn.put("shopping_list", "ক্রয় তালিকা");
        bn.put("liquid_option", "তরল বিকল্প (Nano Urea)");
        bn.put("ai_advisory", "এআই বিশেষজ্ঞ পরামর্শ");
        bn.put("urea", "ইউরিয়া");
        bn.put("dap", "ডিএপি");
        bn.put("mop", "এমওপি");
        bn.put("nitrogen", "নাইট্রোজেন");
        bn.put("phosphorus", "ফসফরাস");
        bn.put("potassium", "পটাশিয়াম");
        bn.put("soil_type", "মাটির ধরন");
        bn.put("irrigation_method", "সেচ পদ্ধতি");
        bn.put("critical_stage", "গুরুত্বপূর্ণ পর্যায়");
        bn.put("expert_tip", "বিশেষজ্ঞের পরামর্শ");
        bn.put("irrigation_report", "সেচ পরিকল্পনা রিপোর্ট");
        bn.put("yield_report", "ফলন অনুমান রিপোর্ট");
        bn.put("mandi_report", "মান্ডি দর রিপোর্ট");
        bn.put("diagnosis_report", "ফসলের রোগ নির্ণয় রিপোর্ট");
        bn.put("voice_report", "ভয়েস সহকারী রিপোর্ট");
        bn.put("share_report", "রিপোর্ট শেয়ার করুন");
        bn.put("share_text", "টেক্সট হিসেবে শেয়ার করুন");
        bn.put("share_pdf", "পিডিএফ হিসেবে শেয়ার করুন");
        bn.put("cancel", "বাতিল");
        bn.put("settings", "সেটিংস");
        bn.put("language", "ভাষা");
        bn.put("photo_expert", "ফটো বিশেষজ্ঞ");
        bn.put("capture", "ছবি তুলুন");
        bn.put("upload", "আপলোড করুন");
        bn.put("analyze_photo", "ছবি বিশ্লেষণ করুন");
        bn.put("date", "তারিখ");
        bn.put("generated_via", "জয় কিষাণ অ্যাপ দ্বারা তৈরি • ভারতীয় কৃষির পাশে");
        bn.put("home_detect_disease", "রোগ সনাক্তকরণ");
        bn.put("home_voice_assistant", "ভয়েস সহকারী");
        bn.put("home_mandi_prices", "মান্ডির দর");
        bn.put("home_how_to_use", "ব্যবহার নির্দেশিকা");
        bn.put("home_weather_info", "আবহাওয়া তথ্য");
        bn.put("home_farming_tools", "কৃষি সরঞ্জাম");
        bn.put("home_about", "পরিচিতি");
        bn.put("fertilizer_calculator", "সার ক্যালকুলেটর");
        bn.put("yield_estimator", "ফলন অনুমান");
        bn.put("irrigation_planner", "সেচ পরিকল্পনা");
        bn.put("cost_calculator", "খরচ ক্যালকুলেটর");
        DICTIONARY.put("bn", bn);

        // Telugu
        Map<String, String> te = new HashMap<>();
        te.put("jai_kisan", "జై కిసాన్");
        te.put("app_motto", "నా మట్టి, నా రైతు");
        te.put("financial_pro_report", "ఫైనాన్షియల్ ప్రో రిపోర్ట్");
        te.put("crop", "పంట");
        te.put("state", "రాష్ట్రం");
        te.put("total_cost", "మొత్తం ఖర్చు");
        te.put("cost_breakdown", "ఖర్చుల వివరాలు");
        te.put("harvest_estimation", "దిగుబడి మరియు ఆదాయం");
        te.put("seed", "విత్తనాలు");
        te.put("fertilizer", "ఎరువులు");
        te.put("labor", "కూలీ");
        te.put("other", "ఇతర");
        te.put("total_yield", "మొత్తం దిగుబడి");
        te.put("price", "ధర");
        te.put("total_revenue", "మొత్తం ఆదాయం");
        te.put("net_profit", "నికర లాభం");
        te.put("profit_margin", "లాభాల శాతం");
        te.put("break_even", "ఖర్చు సరితూగే ధర");
        te.put("good_crop_choice", "మంచి పంట ఎంపిక");
        te.put("land_area", "భూమి విస్తీర్ణం");
        te.put("recommended_fertilizer", "సిఫార్సు చేసిన ఎరువులు");
        te.put("shopping_list", "కొనుగోలు జాబితా");
        te.put("liquid_option", "ద్రవ ఎంపిక (Nano Urea)");
        te.put("ai_advisory", "ఏఐ నిపుణుల సలహా");
        te.put("urea", "యూరియా");
        te.put("dap", "డిఎపి");
        te.put("mop", "ఎంఓపి");
        te.put("nitrogen", "నత్రజని");
        te.put("phosphorus", "భాస్వరం");
        te.put("potassium", "పొటాషియం");
        te.put("soil_type", "నేల రకం");
        te.put("irrigation_method", "నీటిపారుదల విధానం");
        te.put("critical_stage", "ముఖ్యమైన దశ");
        te.put("expert_tip", "నిపుణుల సూచన");
        te.put("irrigation_report", "నీటిపారుదల నివేదిక");
        te.put("yield_report", "దిగుబడి అంచనా నివేదిక");
        te.put("mandi_report", "మార్కెట్ ధరల నివేదిక");
        te.put("diagnosis_report", "పంట ఆరోగ్య నివేదిక");
        te.put("voice_report", "వాయిస్ అసిస్టెంట్ నివేదిక");
        te.put("share_report", "నివేదికను భాగస్వామ్యం చేయండి");
        te.put("share_text", "వచనంగా భాగస్వామ్యం చేయండి");
        te.put("share_pdf", "పిడిఎఫ్ గా భాగస్వామ్యం చేయండి");
        te.put("cancel", "రద్దు చేయండి");
        te.put("settings", "సెట్టింగులు");
        te.put("language", "భాష");
        te.put("photo_expert", "ఫోటో నిపుణుడు");
        te.put("capture", "ఫోటో తీయండి");
        te.put("upload", "అప్‌లోడ్ చేయండి");
        te.put("analyze_photo", "ఫోటోను విశ్లేషించండి");
        te.put("date", "తేదీ");
        te.put("generated_via", "జై కిసాన్ యాప్ ద్వారా అందించబడింది • భారతీయ రైతులకు అండగా");
        te.put("home_detect_disease", "వ్యాధి నిర్ధారణ");
        te.put("home_voice_assistant", "వాయిస్ అసిస్టెంట్");
        te.put("home_mandi_prices", "మార్కెట్ ధరలు");
        te.put("home_how_to_use", "ఎలా ఉపయోగించాలి");
        te.put("home_weather_info", "వాతావరణ సమాచారం");
        te.put("home_farming_tools", "వ్యవసాయ పరికరాలు");
        te.put("home_about", "గురించి");
        te.put("fertilizer_calculator", "ఎరువుల కాలిక్యులేటర్");
        te.put("yield_estimator", "దిగుబడి అంచనా");
        te.put("irrigation_planner", "నీటిపారుదల ప్రణాళిక");
        te.put("cost_calculator", "ఖర్చు కాలిక్యులేటర్");
        DICTIONARY.put("te", te);

        // Marathi
        Map<String, String> mr = new HashMap<>();
        mr.put("jai_kisan", "जय किसान");
        mr.put("app_motto", "माझी माती, माझे शेतकरी");
        mr.put("financial_pro_report", "आर्थिक प्रो अहवाल");
        mr.put("crop", "पीक");
        mr.put("state", "राज्य");
        mr.put("total_cost", "एकूण खर्च");
        mr.put("cost_breakdown", "खर्चाचा तपशील");
        mr.put("harvest_estimation", "उत्पादन आणि महसूल");
        mr.put("seed", "बियाणे");
        mr.put("fertilizer", "खत");
        mr.put("labor", "मजूर");
        mr.put("other", "इतर");
        mr.put("total_yield", "एकूण उत्पादन");
        mr.put("price", "दर");
        mr.put("total_revenue", "एकूण महसूल");
        mr.put("net_profit", "निव्वळ नफा");
        mr.put("profit_margin", "नफा प्रमाण");
        mr.put("break_even", "किमान परतावा दर");
        mr.put("good_crop_choice", "योग्य पीक निवड");
        mr.put("land_area", "जमीन क्षेत्र");
        mr.put("recommended_fertilizer", "शिफारस केलेले खत");
        mr.put("shopping_list", "खरेदी यादी");
        mr.put("liquid_option", "द्रव पर्याय (Nano Urea)");
        mr.put("ai_advisory", "एआय तज्ज्ञ सल्ला");
        mr.put("urea", "युरिया");
        mr.put("dap", "डीएपी");
        mr.put("mop", "एमओपी");
        mr.put("nitrogen", "नायट्रोजन");
        mr.put("phosphorus", "फॉस्फरस");
        mr.put("potassium", "पोटॅशियम");
        mr.put("soil_type", "मातीचा प्रकार");
        mr.put("irrigation_method", "सिंचन पद्धत");
        mr.put("critical_stage", "महत्त्वाचा टप्पा");
        mr.put("expert_tip", "तज्ज्ञ सल्ला");
        mr.put("irrigation_report", "सिंचन योजना अहवाल");
        mr.put("yield_report", "उत्पादन अंदाज अहवाल");
        mr.put("mandi_report", "बाजार भाव अहवाल");
        mr.put("diagnosis_report", "पीक रोग निदान अहवाल");
        mr.put("voice_report", "व्हॉइस असिस्टंट अहवाल");
        mr.put("share_report", "अहवाल शेअर करा");
        mr.put("share_text", "मजकूर म्हणून शेअर करा");
        mr.put("share_pdf", "पीडीएफ म्हणून शेअर करा");
        mr.put("cancel", "रद्द करा");
        mr.put("settings", "सेटिंग्ज");
        mr.put("language", "भाषा");
        mr.put("photo_expert", "फोटो तज्ज्ञ");
        mr.put("capture", "फोटो काढा");
        mr.put("upload", "अपलोड करा");
        mr.put("analyze_photo", "फोटोचे विश्लेषण करा");
        mr.put("date", "दिनांक");
        mr.put("generated_via", "जय किसान ॲपद्वारे निर्मित • भारतीय शेतीचा आधार");
        mr.put("home_detect_disease", "रोग निदान");
        mr.put("home_voice_assistant", "ध्वनी सहाय्यक");
        mr.put("home_mandi_prices", "बाजार भाव");
        mr.put("home_how_to_use", "कसे वापरावे");
        mr.put("home_weather_info", "हवामान माहिती");
        mr.put("home_farming_tools", "शेती साधने");
        mr.put("home_about", "माहिती");
        mr.put("fertilizer_calculator", "खत कॅल्क्युलेटर");
        mr.put("yield_estimator", "उत्पादन अंदाज");
        mr.put("irrigation_planner", "सिंचन नियोजन");
        mr.put("cost_calculator", "खर्च कॅल्क्युलेटर");
        DICTIONARY.put("mr", mr);

        // Tamil
        Map<String, String> ta = new HashMap<>();
        ta.put("jai_kisan", "ஜெய் கிசான்");
        ta.put("app_motto", "என் மண், என் உழவர்");
        ta.put("financial_pro_report", "நிதி சார்பு அறிக்கை");
        ta.put("crop", "பயிர்");
        ta.put("state", "மாநிலம்");
        ta.put("total_cost", "மொத்த செலவு");
        ta.put("cost_breakdown", "செலவு விவரங்கள்");
        ta.put("harvest_estimation", "விளைச்சல் மற்றும் வருவாய்");
        ta.put("seed", "விதை");
        ta.put("fertilizer", "உரம்");
        ta.put("labor", "கூலி");
        ta.put("other", "மற்றவை");
        ta.put("total_yield", "மொத்த விளைச்சல்");
        ta.put("price", "விலை");
        ta.put("total_revenue", "மொத்த வருவாய்");
        ta.put("net_profit", "நிகர லாபம்");
        ta.put("profit_margin", "லாப வரம்பு");
        ta.put("break_even", "செலவு ஈடு செய்யும் விலை");
        ta.put("good_crop_choice", "நல்ல பயிர் தேர்வு");
        ta.put("land_area", "நிலப்பரப்பு");
        ta.put("recommended_fertilizer", "பரிந்துரைக்கப்பட்ட உரம்");
        ta.put("shopping_list", "கொள்முதல் பட்டியல்");
        ta.put("liquid_option", "திரவ விருப்பம் (Nano Urea)");
        ta.put("ai_advisory", "AI நிபுணர் ஆலோசனை");
        ta.put("urea", "யூரியா");
        ta.put("dap", "டிஏபி");
        ta.put("mop", "எம்ஓபி");
        ta.put("nitrogen", "நைட்ரஜன்");
        ta.put("phosphorus", "பாஸ்பரஸ்");
        ta.put("potassium", "பொட்டாசியம்");
        ta.put("soil_type", "மண் வகை");
        ta.put("irrigation_method", "நீர்ப்பாசன முறை");
        ta.put("critical_stage", "முக்கியமான நிலை");
        ta.put("expert_tip", "நிபுணர் குறிப்பு");
        ta.put("irrigation_report", "நீர்ப்பாசன திட்டம் அறிக்கை");
        ta.put("yield_report", "விளைச்சல் மதிப்பீட்டு அறிக்கை");
        ta.put("mandi_report", "சந்தை விலை அறிக்கை");
        ta.put("diagnosis_report", "பயிர் நோய் கண்டறிதல் அறிக்கை");
        ta.put("voice_report", "குரல் உதவியாளர் அறிக்கை");
        ta.put("share_report", "அறிக்கையைப் பகிரவும்");
        ta.put("share_text", "உரையாகப் பகிரவும்");
        ta.put("share_pdf", "PDF ஆகப் பகிரவும்");
        ta.put("cancel", "ரத்து செய்");
        ta.put("settings", "அமைப்புகள்");
        ta.put("language", "மொழி");
        ta.put("photo_expert", "புகைப்பட நிபுணர்");
        ta.put("capture", "படம் பிடி");
        ta.put("upload", "பதிவேற்று");
        ta.put("analyze_photo", "பகுப்பாய்வு செய்");
        ta.put("date", "தேதி");
        ta.put("generated_via", "ஜெய் கிசான் செயலி மூலம் உருவாக்கப்பட்டது • இந்திய விவசாயத்திற்கு துணை");
        ta.put("home_detect_disease", "நோய் கண்டறிதல்");
        ta.put("home_voice_assistant", "குரல் உதவியாளர்");
        ta.put("home_mandi_prices", "சந்தை விலைகள்");
        ta.put("home_how_to_use", "எப்படி உபயோகிப்பது");
        ta.put("home_weather_info", "வானிலை தகவல்");
        ta.put("home_farming_tools", "விவசாய கருவிகள்");
        ta.put("home_about", "பற்றி");
        ta.put("fertilizer_calculator", "உர கணக்கீடு");
        ta.put("yield_estimator", "மகசூல் மதிப்பீடு");
        ta.put("irrigation_planner", "நீர்ப்பாசன திட்டம்");
        ta.put("cost_calculator", "செலவு கணக்கீடு");
        DICTIONARY.put("ta", ta);

        // Gujarati
        Map<String, String> gu = new HashMap<>();
        gu.put("jai_kisan", "જય કિસાન");
        gu.put("app_motto", "મારી માટી, મારા કિસાન");
        gu.put("financial_pro_report", "નાણાકીય પ્રો અહેવાલ");
        gu.put("crop", "પાક");
        gu.put("state", "રાજ્ય");
        gu.put("total_cost", "કુલ ખર્ચ");
        gu.put("cost_breakdown", "ખર્ચની વિગત");
        gu.put("harvest_estimation", "ઉત્પાદન અને આવક");
        gu.put("seed", "બીજ");
        gu.put("fertilizer", "ખાતર");
        gu.put("labor", "મજૂર");
        gu.put("other", "અન્ય");
        gu.put("total_yield", "કુલ ઉત્પાદન");
        gu.put("price", "ભાવ");
        gu.put("total_revenue", "કુલ આવક");
        gu.put("net_profit", "ચોખ્ખો નફો");
        gu.put("profit_margin", "નફાનું પ્રમાણ");
        gu.put("break_even", "ખર્ચ સરભર ભાવ");
        gu.put("good_crop_choice", "યોગ્ય પાકની પસંદગી");
        gu.put("land_area", "જમીન વિસ્તાર");
        gu.put("recommended_fertilizer", "ભલામણ કરેલ ખાતર");
        gu.put("shopping_list", "ખરીદી યાદી");
        gu.put("liquid_option", "પ્રવાહી વિકલ્પ (Nano Urea)");
        gu.put("ai_advisory", "AI નિષ્ણાત સલાહ");
        gu.put("urea", "યુરિયા");
        gu.put("dap", "ડીએપી");
        gu.put("mop", "એમઓપી");
        gu.put("nitrogen", "નાઇટ્રોજન");
        gu.put("phosphorus", "ફોસ્ફરસ");
        gu.put("potassium", "પોટેશિયમ");
        gu.put("soil_type", "માટીનો પ્રકાર");
        gu.put("irrigation_method", "સિંચાઈ પદ્ધતિ");
        gu.put("critical_stage", "મહત્વપૂર્ણ તબક્કો");
        gu.put("expert_tip", "નિષ્ણાત સલાહ");
        gu.put("irrigation_report", "સિંચાઈ યોજના અહેવાલ");
        gu.put("yield_report", "ઉત્પાદન અંદાજ અહેવાલ");
        gu.put("mandi_report", "માર્કેટ યાર્ડ ભાવ અહેવાલ");
        gu.put("diagnosis_report", "પાક રોગ નિદાન અહેવાલ");
        gu.put("voice_report", "વોઇસ સહાયક અહેવાલ");
        gu.put("share_report", "અહેવાલ શેર કરો");
        gu.put("share_text", "ટેક્સ્ટ તરીકે શેર કરો");
        gu.put("share_pdf", "પીડીએફ તરીકે શેર કરો");
        gu.put("cancel", "રદ કરો");
        gu.put("settings", "સેટિંગ્સ");
        gu.put("language", "ભાષા");
        gu.put("photo_expert", "ફોટો નિષ્ણાત");
        gu.put("capture", "ફોટો લો");
        gu.put("upload", "અપલોડ કરો");
        gu.put("analyze_photo", "ફોટોનું વિશ્લેષણ કરો");
        gu.put("date", "તારીખ");
        gu.put("generated_via", "જય કિસાન એપ દ્વારા નિર્મિત • ભારતીય ખેતીનો આધાર");
        gu.put("home_detect_disease", "રોગ ઓળખ");
        gu.put("home_voice_assistant", "વોઇસ સહાયક");
        gu.put("home_mandi_prices", "માર્કેટ ભાવ");
        gu.put("home_how_to_use", "કેવી રીતે વાપરવું");
        gu.put("home_weather_info", "હવામાન માહિતી");
        gu.put("home_farming_tools", "ખેતી ઓજારો");
        gu.put("home_about", "વિશે");
        gu.put("fertilizer_calculator", "ખાતર કેલ્ક્યુલેટર");
        gu.put("yield_estimator", "ઉપજ અંદાજ");
        gu.put("irrigation_planner", "સિંચાઈ યોજના");
        gu.put("cost_calculator", "ખર્ચ કેલ્ક્યુલેટર");
        DICTIONARY.put("gu", gu);

        // Kannada
        Map<String, String> kn = new HashMap<>();
        kn.put("jai_kisan", "ಜೈ ಕಿಸಾನ್");
        kn.put("app_motto", "ನನ್ನ ಮಣ್ಣು, ನನ್ನ ರೈತ");
        kn.put("financial_pro_report", "ಹಣಕಾಸು ಪ್ರೊ ವರದಿ");
        kn.put("crop", "ಬೆಳೆ");
        kn.put("state", "ರಾಜ್ಯ");
        kn.put("total_cost", "ಒಟ್ಟು ವೆಚ್ಚ");
        kn.put("cost_breakdown", "ವೆಚ್ಚದ ವಿವರಣೆ");
        kn.put("harvest_estimation", "ಇಳುವರಿ ಮತ್ತು ಆದಾಯ");
        kn.put("seed", "ಬೀಜ");
        kn.put("fertilizer", "ಗೊಬ್ಬರ");
        kn.put("labor", "ಕೂಲಿ");
        kn.put("other", "ಇತರೆ");
        kn.put("total_yield", "ಒಟ್ಟು ಇಳುವರಿ");
        kn.put("price", "ಬೆಲೆ");
        kn.put("total_revenue", "ಒಟ್ಟು ಆದಾಯ");
        kn.put("net_profit", "ನಿವ್ವಳ ಲಾಭ");
        kn.put("profit_margin", "ಲಾಭದ ಪ್ರಮಾಣ");
        kn.put("break_even", "ವೆಚ್ಚ ಸಮತೋಲನ ಬೆಲೆ");
        kn.put("good_crop_choice", "ಉತ್ತಮ ಬೆಳೆ ಆಯ್ಕೆ");
        kn.put("land_area", "ಜಮೀನಿನ ವಿಸ್ತೀರ್ಣ");
        kn.put("recommended_fertilizer", "ಶಿಫಾರಸು ಮಾಡಿದ ಗೊಬ್ಬರ");
        kn.put("shopping_list", "ಖರೀದಿ ಪಟ್ಟಿ");
        kn.put("liquid_option", "ದ್ರವ ರೂಪದ ಆಯ್ಕೆ (Nano Urea)");
        kn.put("ai_advisory", "ಎಐ ತಜ್ಞರ ಸಲಹೆ");
        kn.put("urea", "ಯೂರಿಯಾ");
        kn.put("dap", "ಡಿಎಪಿ");
        kn.put("mop", "ಎಂಒಪಿ");
        kn.put("nitrogen", "ಸಾರಜನಕ");
        kn.put("phosphorus", "ರಂಜಕ");
        kn.put("potassium", "ಪೊಟ್ಯಾಶಿಯಂ");
        kn.put("soil_type", "ಮಣ್ಣಿನ ವಿಧ");
        kn.put("irrigation_method", "ನೀರಾವರಿ ವಿಧಾನ");
        kn.put("critical_stage", "ಪ್ರಮುಖ ಹಂತ");
        kn.put("expert_tip", "ತಜ್ಞರ ಸಲಹೆ");
        kn.put("irrigation_report", "ನೀರಾವರಿ ಯೋಜನೆ ವರದಿ");
        kn.put("yield_report", "ಇಳುವರಿ ಅಂದಾಜು ವರದಿ");
        kn.put("mandi_report", "ಮಾರುಕಟ್ಟೆ ಬೆಲೆ ವರದಿ");
        kn.put("diagnosis_report", "ಬೆಳೆ ರೋಗ ತಪಾಸಣೆ ವರದಿ");
        kn.put("voice_report", "ಧ್ವನಿ ಸಹಾಯಕ ವರದಿ");
        kn.put("share_report", "ವರದಿ ಹಂಚಿಕೊಳ್ಳಿ");
        kn.put("share_text", "ಪಠ್ಯವಾಗಿ ಹಂಚಿಕೊಳ್ಳಿ");
        kn.put("share_pdf", "ಪಿಡಿಎಫ್ ಆಗಿ ಹಂಚಿಕೊಳ್ಳಿ");
        kn.put("cancel", "ರದ್ದುಮಾಡಿ");
        kn.put("settings", "ಸೆಟ್ಟಿಂಗ್ಸ್");
        kn.put("language", "ಭಾಷೆ");
        kn.put("photo_expert", "ಫೋಟೋ ತಜ್ಞ");
        kn.put("capture", "ಫೋಟೋ ತೆಗೆಯಿರಿ");
        kn.put("upload", "ಅಪ್‌ಲೋಡ್ ಮಾಡಿ");
        kn.put("analyze_photo", "ಫೋಟೋ ವಿಶ್ಲೇಷಿಸಿ");
        kn.put("date", "ದಿನಾಂಕ");
        kn.put("generated_via", "ಜೈ ಕಿಸಾನ್ ಅಪ್ಲಿಕೇಶನ್ ಮೂಲಕ ರಚಿಸಲಾಗಿದೆ • ಭಾರತೀಯ ರೈತರಿಗೆ ಬೆಂಬಲ");
        kn.put("home_detect_disease", "ರೋಗ ಪತ್ತೆ");
        kn.put("home_voice_assistant", "ಧ್ವನಿ ಸಹಾಯಕ");
        kn.put("home_mandi_prices", "ಮಾರುಕಟ್ಟೆ ದರ");
        kn.put("home_how_to_use", "ಬಳಸುವುದು ಹೇಗೆ");
        kn.put("home_weather_info", "ಹವಾಮಾನ ಮಾಹಿತಿ");
        kn.put("home_farming_tools", "ಕೃಷಿ ಉಪಕರಣಗಳು");
        kn.put("home_about", "ಕುರಿತು");
        kn.put("fertilizer_calculator", "ಗೊಬ್ಬರ ಕ್ಯಾಲ್ಕುಲೇಟರ್");
        kn.put("yield_estimator", "ಇಳುವರಿ ಅಂದಾಜು");
        kn.put("irrigation_planner", "ನೀರಾವರಿ ಯೋಜನೆ");
        kn.put("cost_calculator", "ವೆಚ್ಚ ಕ್ಯಾಲ್ಕುಲೇಟರ್");
        DICTIONARY.put("kn", kn);

        // Malayalam
        Map<String, String> ml = new HashMap<>();
        ml.put("jai_kisan", "ജയ് കിസാൻ");
        ml.put("app_motto", "എന്റെ മണ്ണ്, എന്റെ കർഷകൻ");
        ml.put("financial_pro_report", "ഫിനാൻഷ്യൽ പ്രോ റിപ്പോർട്ട്");
        ml.put("crop", "വിള");
        ml.put("state", "സംസ്ഥാനം");
        ml.put("total_cost", "ആകെ ചിലവ്");
        ml.put("cost_breakdown", "ചിലവ് വിവരങ്ങൾ");
        ml.put("harvest_estimation", "വിളവും വരുമാനവും");
        ml.put("seed", "വിത്ത്");
        ml.put("fertilizer", "വളം");
        ml.put("labor", "തൊഴിലാളി");
        ml.put("other", "മറ്റുള്ളവ");
        ml.put("total_yield", "ആകെ വിളവ്");
        ml.put("price", "വില");
        ml.put("total_revenue", "ആകെ വരുമാനം");
        ml.put("net_profit", "അറ്റാദായം");
        ml.put("profit_margin", "ലാഭ പരിധി");
        ml.put("break_even", "ചിലവ് നേടുന്ന വില");
        ml.put("good_crop_choice", "നല്ല വിള തിരഞ്ഞെടുപ്പ്");
        ml.put("land_area", "ഭൂവിസ്തൃതി");
        ml.put("recommended_fertilizer", "ശുപാർശ ചെയ്ത വളം");
        ml.put("shopping_list", "വാങ്ങൽ പട്ടിക");
        ml.put("liquid_option", "ദ്രാവക ഓപ്ഷൻ (Nano Urea)");
        ml.put("ai_advisory", "എഐ വിദഗ്ധ ഉപദേശം");
        ml.put("urea", "യൂറിയ");
        ml.put("dap", "ഡിഎപി");
        ml.put("mop", "എംഒപി");
        ml.put("nitrogen", "നൈട്രജൻ");
        ml.put("phosphorus", "ഫോസ്ഫറസ്");
        ml.put("potassium", "പൊട്ടാസ്യം");
        ml.put("soil_type", "മണ്ണിന്റെ തരം");
        ml.put("irrigation_method", "നന രീതി");
        ml.put("critical_stage", "നിർണായക ഘട്ടം");
        ml.put("expert_tip", "വിദഗ്ധോപദേശം");
        ml.put("irrigation_report", "ജലസേചന പദ്ധതി റിപ്പോർട്ട്");
        ml.put("yield_report", "വിളവ് എസ്റ്റിമേറ്റ് റിപ്പോർട്ട്");
        ml.put("mandi_report", "മാർക്കറ്റ് വില റിപ്പോർട്ട്");
        ml.put("diagnosis_report", "വിള രോഗനിർണയ റിപ്പോർട്ട്");
        ml.put("voice_report", "വോയ്‌സ് അസിസ്റ്റന്റ് റിപ്പോർട്ട്");
        ml.put("share_report", "റിപ്പോർട്ട് പങ്കിടുക");
        ml.put("share_text", "ടെക്സ്റ്റായി പങ്കിടുക");
        ml.put("share_pdf", "പിഡിഎഫായി പങ്കിടുക");
        ml.put("cancel", "റദ്ദാക്കുക");
        ml.put("settings", "ക്രമീകരണങ്ങൾ");
        ml.put("language", "ഭാഷ");
        ml.put("photo_expert", "ഫോട്ടോ വിദഗ്ദ്ധൻ");
        ml.put("capture", "ഫോട്ടോ എടുക്കുക");
        ml.put("upload", "അപ്‌ലോഡ് ചെയ്യുക");
        ml.put("analyze_photo", "ഫോട്ടോ വിശകലനം ചെയ്യുക");
        ml.put("date", "തീയതി");
        ml.put("generated_via", "ജയ് കിസാൻ ആപ്പ് വഴി നിർമ്മിച്ചത് • ഇന്ത്യൻ കർഷകർക്ക് പിന്തുണ");
        ml.put("home_detect_disease", "രോഗ നിർണ്ണയം");
        ml.put("home_voice_assistant", "വോയ്‌സ് അസിസ്റ്റന്റ്");
        ml.put("home_mandi_prices", "വിപണി നിരക്കുകൾ");
        ml.put("home_how_to_use", "എങ്ങനെ ഉപയോഗിക്കാം");
        ml.put("home_weather_info", "കാലാവസ്ഥ വിവരം");
        ml.put("home_farming_tools", "കാർഷിക ഉപകരണങ്ങൾ");
        ml.put("home_about", "കുറിച്ച്");
        ml.put("fertilizer_calculator", "വള കാൽക്കുലേറ്റർ");
        ml.put("yield_estimator", "വിളവ് കണക്കാക്കൽ");
        ml.put("irrigation_planner", "ജലസേചന പദ്ധതി");
        ml.put("cost_calculator", "ചിലവ് കാൽക്കുലേറ്റർ");
        DICTIONARY.put("ml", ml);

        // Punjabi
        Map<String, String> pa = new HashMap<>();
        pa.put("jai_kisan", "ਜੈ ਕਿਸਾਨ");
        pa.put("app_motto", "ਮੇਰੀ ਮਿੱਟੀ, ਮੇਰੇ ਕਿਸਾਨ");
        pa.put("financial_pro_report", "ਵਿੱਤੀ ਪ੍ਰੋ ਰਿਪੋਰਟ");
        pa.put("crop", "ਫਸਲ");
        pa.put("state", "ਰਾਜ");
        pa.put("total_cost", "ਕੁੱਲ ਲਾਗਤ");
        pa.put("cost_breakdown", "ਲਾਗਤ ਦਾ ਵੇਰਵਾ");
        pa.put("harvest_estimation", "ਝਾੜ ਅਤੇ ਆਮਦਨ");
        pa.put("seed", "ਬੀਜ");
        pa.put("fertilizer", "ਖਾਦ");
        pa.put("labor", "ਮਜ਼ਦੂਰ");
        pa.put("other", "ਹੋਰ");
        pa.put("total_yield", "ਕੁੱਲ ਝਾੜ");
        pa.put("price", "ਭਾਅ");
        pa.put("total_revenue", "ਕੁੱਲ ਆਮਦਨ");
        pa.put("net_profit", "ਸ਼ੁੱਧ ਲਾਭ");
        pa.put("profit_margin", "ਮੁਨਾਫਾ ਮਾਰਜਿਨ");
        pa.put("break_even", "ਖਰਚਾ ਬਰਾਬਰ ਭਾਅ");
        pa.put("good_crop_choice", "ਵਧੀਆ ਫਸਲ ਚੋਣ");
        pa.put("land_area", "ਜ਼ਮੀਨ");
        pa.put("recommended_fertilizer", "ਸਿਫਾਰਸ਼ ਕੀਤੀ ਖਾਦ");
        pa.put("shopping_list", "ਖਰੀਦ ਸੂਚੀ");
        pa.put("liquid_option", "ਤਰਲ ਵਿਕਲਪ (Nano Urea)");
        pa.put("ai_advisory", "ਏਆਈ ਮਾਹਿਰ ਸਲਾਹ");
        pa.put("urea", "ਯੂਰੀਆ");
        pa.put("dap", "ਡੀਏਪੀ");
        pa.put("mop", "ਐਮਓਪੀ");
        pa.put("nitrogen", "ਨਾਈਟ੍ਰੋਜਨ");
        pa.put("phosphorus", "ਫਾਸਫੋਰਸ");
        pa.put("potassium", "ਪੋਟਾਸ਼ੀਅਮ");
        pa.put("soil_type", "ਮਿੱਟੀ ਦੀ ਕਿਸਮ");
        pa.put("irrigation_method", "ਸਿੰਚਾਈ ਢੰਗ");
        pa.put("critical_stage", "ਮਹੱਤਵਪੂਰਨ ਪੜਾਅ");
        pa.put("expert_tip", "ਮਾਹਿਰ ਸਲਾਹ");
        pa.put("irrigation_report", "ਸਿੰਚਾਈ ਯੋਜਨਾ ਰਿਪੋਰਟ");
        pa.put("yield_report", "ਝਾੜ ਅਨੁਮਾਨ ਰਿਪੋਰਟ");
        pa.put("mandi_report", "ਮੰਡੀ ਭਾਅ ਰਿਪੋਰਟ");
        pa.put("diagnosis_report", "ਫਸਲ ਬਿਮਾਰੀ ਜਾਂਚ ਰਿਪੋਰਟ");
        pa.put("voice_report", "ਆਵਾਜ਼ ਸਹਾਇਕ ਰਿਪੋਰਟ");
        pa.put("share_report", "ਰਿਪੋਰਟ ਸਾਂਝੀ ਕਰੋ");
        pa.put("share_text", "ਟੈਕਸਟ ਵਜੋਂ ਸਾਂਝਾ ਕਰੋ");
        pa.put("share_pdf", "ਪੀਡੀਐਫ ਵਜੋਂ ਸਾਂਝਾ ਕਰੋ");
        pa.put("cancel", "ਰੱਦ ਕਰੋ");
        pa.put("settings", "ਸੈਟਿੰਗਾਂ");
        pa.put("language", "ਭਾਸ਼ਾ");
        pa.put("photo_expert", "ਫੋਟੋ ਮਾਹਿਰ");
        pa.put("capture", "ਫੋਟੋ ਖਿੱਚੋ");
        pa.put("upload", "ਅਪਲੋਡ ਕਰੋ");
        pa.put("analyze_photo", "ਫੋਟੋ ਦੀ ਜਾਂਚ ਕਰੋ");
        pa.put("date", "ਮਿਤੀ");
        pa.put("generated_via", "ਜੈ ਕਿਸਾਨ ਐਪ ਰਾਹੀਂ ਤਿਆਰ • ਭਾਰਤੀ ਕਿਸਾਨਾਂ ਦਾ ਸਹਾਰਾ");
        pa.put("home_detect_disease", "ਰੋਗ ਦੀ ਪਛਾਣ");
        pa.put("home_voice_assistant", "ਅਵਾਜ਼ ਸਹਾਇਕ");
        pa.put("home_mandi_prices", "ਮੰਡੀ ਦੇ ਭਾਅ");
        pa.put("home_how_to_use", "ਕਿਵੇਂ ਵਰਤਣਾ ਹੈ");
        pa.put("home_weather_info", "ਮੌਸਮ ਜਾਣਕਾਰੀ");
        pa.put("home_farming_tools", "ਖੇਤੀਬਾੜੀ ਸੰਦ");
        pa.put("home_about", "ਬਾਰੇ");
        pa.put("fertilizer_calculator", "ਖਾਦ ਕੈਲਕੁਲੇਟਰ");
        pa.put("yield_estimator", "ਝਾੜ ਦਾ ਅੰਦਾਜ਼ਾ");
        pa.put("irrigation_planner", "ਸਿੰਚਾਈ ਯੋਜਨਾ");
        pa.put("cost_calculator", "ਲਾਗਤ ਕੈਲਕੁਲੇਟਰ");
        DICTIONARY.put("pa", pa);
    }

    /**
     * Returns "English / Regional" or "English" if "en" is selected.
     */
    public static String getDual(Context context, String english, String key) {
        String lang = getLanguage(context);
        if ("en".equalsIgnoreCase(lang) || english == null) {
            return english != null ? english : "";
        }

        Map<String, String> langMap = DICTIONARY.get(lang.toLowerCase());
        if (langMap == null) {
            langMap = DICTIONARY.get("hi"); // Fallback to Hindi
        }

        String regional = (langMap != null) ? langMap.get(key) : null;
        if (regional == null && !lang.equalsIgnoreCase("hi")) {
            Map<String, String> hiMap = DICTIONARY.get("hi");
            if (hiMap != null) regional = hiMap.get(key);
        }

        if (regional == null || regional.trim().isEmpty()) {
            return english;
        }

        return english + " / " + regional;
    }

    /**
     * Formats an English string and regional string according to active language.
     * If 'en' is chosen, returns english only.
     * Otherwise returns english + " / " + regional.
     */
    public static String formatDual(Context context, String english, String regional) {
        if (english == null) return regional != null ? regional : "";
        String lang = getLanguage(context);
        if ("en".equalsIgnoreCase(lang)) {
            return english;
        }
        if (regional == null || regional.trim().isEmpty() || regional.equalsIgnoreCase(english)) {
            return english;
        }
        return english + " / " + regional;
    }

    /**
     * Helper to format a crop name cleanly given english and optional regional name.
     */
    public static String getLocalizedCropName(Context context, String cropEn, String cropHi) {
        if (cropEn == null || cropEn.trim().isEmpty()) {
            return cropHi != null ? cropHi : "Crop";
        }
        return formatDual(context, cropEn.trim(), cropHi != null ? cropHi.trim() : null);
    }

    /**
     * Returns regional translation only (or English if 'en' selected).
     */
    public static String getRegional(Context context, String key, String defaultEnglish) {
        String lang = getLanguage(context);
        if ("en".equalsIgnoreCase(lang)) {
            return defaultEnglish;
        }

        Map<String, String> langMap = DICTIONARY.get(lang.toLowerCase());
        if (langMap == null) langMap = DICTIONARY.get("hi");

        String regional = (langMap != null) ? langMap.get(key) : null;
        return (regional != null) ? regional : defaultEnglish;
    }

    public static class NamedItem {
        public final String en;
        public final String hi;

        public NamedItem(String en, String hi) {
            this.en = en;
            this.hi = hi;
        }
    }

    public static final NamedItem[] DEFAULT_CROPS = {
            new NamedItem("Wheat", "गेहूं"),
            new NamedItem("Rice", "धान"),
            new NamedItem("Maize", "मक्का"),
            new NamedItem("Cotton", "कपास"),
            new NamedItem("Sugarcane", "गन्ना"),
            new NamedItem("Soybean", "सोयाबीन"),
            new NamedItem("Mustard", "सरसों"),
            new NamedItem("Potato", "आलू"),
            new NamedItem("Onion", "प्याज"),
            new NamedItem("Chili", "मिर्च")
    };

    public static final NamedItem[] DEFAULT_STATES = {
            new NamedItem("Andhra Pradesh", "आंध्र प्रदेश"),
            new NamedItem("Arunachal Pradesh", "अरुणाचल प्रदेश"),
            new NamedItem("Assam", "असम"),
            new NamedItem("Bihar", "बिहार"),
            new NamedItem("Chhattisgarh", "छत्तीसगढ़"),
            new NamedItem("Goa", "गोवा"),
            new NamedItem("Gujarat", "गुजरात"),
            new NamedItem("Haryana", "हरियाणा"),
            new NamedItem("Himachal Pradesh", "हिमाचल प्रदेश"),
            new NamedItem("Jharkhand", "झारखंड"),
            new NamedItem("Karnataka", "कर्नाटक"),
            new NamedItem("Kerala", "केरल"),
            new NamedItem("Madhya Pradesh", "मध्य प्रदेश"),
            new NamedItem("Maharashtra", "महाराष्ट्र"),
            new NamedItem("Manipur", "मणिपुर"),
            new NamedItem("Meghalaya", "मेघालय"),
            new NamedItem("Mizoram", "मिजोरम"),
            new NamedItem("Nagaland", "नागालैंड"),
            new NamedItem("Odisha", "ओडिशा"),
            new NamedItem("Punjab", "पंजाब"),
            new NamedItem("Rajasthan", "राजस्थान"),
            new NamedItem("Sikkim", "सिक्किम"),
            new NamedItem("Tamil Nadu", "तमिलनाडु"),
            new NamedItem("Telangana", "तेलंगाना"),
            new NamedItem("Tripura", "त्रिपुरा"),
            new NamedItem("Uttar Pradesh", "उत्तर प्रदेश"),
            new NamedItem("Uttarakhand", "उत्तराखंड"),
            new NamedItem("West Bengal", "पश्चिम बंगाल"),
            new NamedItem("Delhi", "दिल्ली"),
            new NamedItem("Jammu & Kashmir", "जम्मू और कश्मीर"),
            new NamedItem("Ladakh", "लद्दाख")
    };

    public static String[] getLocalizedCrops(Context context) {
        String[] result = new String[DEFAULT_CROPS.length];
        for (int i = 0; i < DEFAULT_CROPS.length; i++) {
            result[i] = formatDual(context, DEFAULT_CROPS[i].en, DEFAULT_CROPS[i].hi);
        }
        return result;
    }

    public static String[] getLocalizedStates(Context context) {
        String[] result = new String[DEFAULT_STATES.length];
        for (int i = 0; i < DEFAULT_STATES.length; i++) {
            result[i] = formatDual(context, DEFAULT_STATES[i].en, DEFAULT_STATES[i].hi);
        }
        return result;
    }
}
