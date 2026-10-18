package com.rcc.jai_kisan;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

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

    public static LangItem getSelectedLanguageItem(Context context) {
        return SUPPORTED_LANGUAGES[getLanguageIndex(context)];
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
        Map<String, String> hi = new HashMap<>();
        hi.put("jai_kisan", "जय किसान");
        hi.put("app_motto", "मेरी मिट्टी, मेरे किसान");
        hi.put("financial_pro_report", "वित्तीय प्रो रिपोर्ट");
        hi.put("crop", "फसल");
        hi.put("state", "राज्य");
        hi.put("city", "जिला / मंडी");
        hi.put("variety", "किस्म");
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
        hi.put("loss", "हानि");
        hi.put("profit_margin", "लाभ प्रतिशत");
        hi.put("break_even", "लागत-वसूली दर");
        hi.put("good_crop_choice", "सही फसल चुनाव");
        hi.put("medium_risk", "मध्यम जोखिम");
        hi.put("warning_risk", "चेतावनी");
        hi.put("safe_harvest_tip", "सुरक्षित पैदावार है लेकिन खर्चों पर ध्यान दें।");
        hi.put("low_margin_tip", "मार्जिन बहुत कम है। जोखिम अधिक है।");
        hi.put("excellent_margin_tip", "आपके इनपुट के आधार पर उत्कृष्ट लाभ की संभावना।");
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
        hi.put("share_pdf", "पीडीएफ साझा करें");
        hi.put("share_option", "साझा करने का विकल्प");
        hi.put("share_text_desc", "रिपोर्ट को संदेश के रूप में साझा करें।");
        hi.put("share_pdf_desc", "एक सुंदर पीडीएफ दस्तावेज साझा करें।");
        hi.put("cancel", "रद्द करें");
        hi.put("settings", "सेटिंग्स");
        hi.put("language", "भाषा");
        hi.put("photo_expert", "फोटो विशेषज्ञ");
        hi.put("detect_desc", "किसी भी फसल, पत्ती या फल का विश्लेषण करें। विशेषज्ञ रिपोर्ट पाने के लिए फोटो खींचें या अपलोड करें।");
        hi.put("no_image_selected", "कोई छवि चयनित नहीं");
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
        hi.put("voice_assistant", "आवाज़ सहायक");
        hi.put("tap_to_ask", "पूछने के लिए टैप करें");
        hi.put("listening", "सुन रहा हूँ...");
        hi.put("thinking", "सोच रहा हूँ...");
        hi.put("voice_greeting", "नमस्ते! मैं आपकी कृषि सहायता के लिए यहाँ हूँ।");
        hi.put("mandi_prices", "मंडी भाव");
        hi.put("select_crop", "फसल चुनें");
        hi.put("select_state", "राज्य चुनें");
        hi.put("select_city", "ज़िला / मंडी चुनें");
        hi.put("select_variety", "किस्म चुनें");
        hi.put("select_date", "तारीख चुनें");
        hi.put("get_prices", "मंडी भाव प्राप्त करें");
        hi.put("market_insight", "बाज़ार जानकारी");
        hi.put("price_range", "मूल्य सीमा");
        hi.put("modal_price", "सामान्य मूल्य");
        hi.put("price_trend", "7-दिनों का मूल्य रुझान");
        hi.put("weather_impact", "मौसम प्रभाव");
        hi.put("farmer_advice", "किसान सलाह");
        hi.put("sort_by", "क्रमबद्ध करें");
        hi.put("date_range", "तारीख सीमा");
        hi.put("production_costs", "1. उत्पादन लागत");
        hi.put("expected_harvest", "2. अनुमानित पैदावार");
        hi.put("financial_analysis", "3. वित्तीय विश्लेषण");
        hi.put("calculate_financials", "लाभ-हानि गणना करें");
        hi.put("estimate_yield", "पैदावार का अनुमान लगाएं");
        hi.put("generate_plan", "सिंचाई योजना बनाएं");
        hi.put("health_status", "स्वास्थ्य स्थिति");
        hi.put("survival_chance", "जीवित रहने की संभावना");
        hi.put("summary", "सारांश");
        hi.put("recommended_actions", "क्या करें (Do's)");
        hi.put("things_to_avoid", "क्या न करें (Don'ts)");
        hi.put("cures_fertilizers", "उपचार एवं उर्वरक");
        hi.put("apply_to_tools", "संबंधित खेती उपकरण");
        hi.put("fertilizer_calc", "उर्वरक कैलकुलेटर");
        hi.put("cost_calc", "लागत कैलकुलेटर");
        hi.put("irrigation_plan", "सिंचाई योजना");
        hi.put("detect_location", "स्थान का पता लगाएं");
        hi.put("weather_title", "मौसम जानकारी");
        hi.put("how_to_use", "उपयोग निर्देशिका");
        hi.put("about_developer", "डेवलपर परिचय");
        hi.put("general_variety", "सामान्य");
        hi.put("other_variety", "अन्य");
        hi.put("alluvial_soil", "जलोढ़");
        hi.put("black_soil", "काली मिट्टी");
        hi.put("red_soil", "लाल मिट्टी");
        hi.put("laterite_soil", "लैटेराइट");
        hi.put("sandy_soil", "रेतीली मिट्टी");
        hi.put("clay_soil", "चिकनी मिट्टी");
        hi.put("flood_irrigation", "सतह सिंचाई");
        hi.put("drip_irrigation", "ड्रिप (टपक)");
        hi.put("sprinkler_irrigation", "फव्वारा");
        hi.put("furrow_irrigation", "कूंड़ सिंचाई");
        hi.put("basin_irrigation", "थाला सिंचाई");
        hi.put("crop_wheat", "गेहूं");
        hi.put("crop_rice", "चावल");
        hi.put("crop_maize", "मक्का");
        hi.put("crop_cotton", "कपास");
        hi.put("crop_sugarcane", "गन्ना");
        hi.put("crop_soybean", "सोयाबीन");
        hi.put("crop_mustard", "सरसों");
        hi.put("crop_potato", "आलू");
        hi.put("crop_onion", "प्याज");
        hi.put("crop_chili", "मिर्च");
        hi.put("crop_tomato", "टमाटर");
        hi.put("crop_paddy", "धान");
        hi.put("crop_millets", "बाजरा");
        hi.put("crop_pulses", "दालें");
        hi.put("acre", "एकड़");
        hi.put("hectare", "हेक्टेयर");
        hi.put("bigha", "बीघा");
        hi.put("kanal", "कनाल");
        hi.put("biswa", "बिस्वा");
        hi.put("cent", "सेंट");
        hi.put("guntha", "गुंठा");
        hi.put("katha", "कट्ठा");
        hi.put("decimal", "डेसीमल");
        hi.put("find_centers", "कृषि केंद्र खोजें");
        hi.put("healthy", "स्वस्थ");
        hi.put("detected", "रोग पहचान");
        hi.put("click_for_info", "अधिक जानकारी के लिए क्लिक करें");
        hi.put("response_time", "प्रतिक्रिया समय: %.1f सेकंड");
        hi.put("note_optional_date", "ध्यान दें: तारीख चुनना वैकल्पिक है।");
        hi.put("order", "क्रम");
        hi.put("avg_yield", "औसत पैदावार");
        hi.put("quintals", "क्विंटल");
        hi.put("quintals_acre", "क्विंटल/एकड़");
        hi.put("irrigation_interval", "सिंचाई का अंतराल");
        hi.put("days", "दिन");
        hi.put("unit", "इकाई");
        hi.put("area", "क्षेत्रफल");
        hi.put("calculate_requirements", "आवश्यकता की गणना करें");
        DICTIONARY.put("hi", hi);

        Map<String, String> bn = new HashMap<>();
        bn.put("jai_kisan", "জয় কিষাণ");
        bn.put("app_motto", "আমার মাটি, আমার কৃষক");
        bn.put("financial_pro_report", "আর্থিক প্রো রিপোর্ট");
        bn.put("crop", "ফসল");
        bn.put("state", "রাজ্য");
        bn.put("city", "জেলা / মান্ডি");
        bn.put("variety", "জাত");
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
        bn.put("loss", "ক্ষতি");
        bn.put("profit_margin", "মুনাফা মার্জিন");
        bn.put("break_even", "ব্রেক-ইভেন দর");
        bn.put("good_crop_choice", "উত্তম ফসল নির্বাচন");
        bn.put("medium_risk", "মাঝারি ঝুঁকি");
        bn.put("warning_risk", "সতর্কতা");
        bn.put("safe_harvest_tip", "নিরাপদ ফলন কিন্তু খরচের দিকে নজর রাখুন।");
        bn.put("low_margin_tip", "মুনাফা মার্জিন খুব কম। উচ্চ ঝুঁকি রয়েছে।");
        bn.put("excellent_margin_tip", "আপনার ইনপুটের ওপর ভিত্তি করে চমৎকার মুনাফার সম্ভাবনা।");
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
        bn.put("share_pdf", "পিডিএফ শেয়ার করুন");
        bn.put("share_option", "শেয়ার বিকল্প");
        bn.put("share_text_desc", "রিপোর্টটি বার্তা হিসেবে শেয়ার করুন।");
        bn.put("share_pdf_desc", "একটি সুন্দর পিডিএফ নথি শেয়ার করুন।");
        bn.put("cancel", "বাতিল");
        bn.put("settings", "সেটিংস");
        bn.put("language", "ভাষা");
        bn.put("photo_expert", "ফটো বিশেষজ্ঞ");
        bn.put("detect_desc", "যেকোনো ফসল, পাতা বা ফলের ছবি তুলুন বা আপলোড করে বিশেষজ্ঞ রিপোর্ট পান।");
        bn.put("no_image_selected", "কোনো ছবি নির্বাচিত হয়নি");
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
        bn.put("voice_assistant", "ভয়েস সহকারী");
        bn.put("tap_to_ask", "জিজ্ঞাসা করতে ট্যাপ করুন");
        bn.put("listening", "শুনছি...");
        bn.put("thinking", "ভাবছি...");
        bn.put("voice_greeting", "নমস্কার! আমি আপনার কৃষি সহায়তার জন্য প্রস্তুত।");
        bn.put("mandi_prices", "মান্ডি দর");
        bn.put("select_crop", "ফসল নির্বাচন করুন");
        bn.put("select_state", "রাজ্য নির্বাচন করুন");
        bn.put("select_city", "জেলা / মান্ডি নির্বাচন করুন");
        bn.put("select_variety", "জাত নির্বাচন করুন");
        bn.put("select_date", "তারিখ নির্বাচন করুন");
        bn.put("get_prices", "মান্ডি দর দেখুন");
        bn.put("market_insight", "বাজার তথ্য");
        bn.put("price_range", "দরের সীমা");
        bn.put("modal_price", "গড় বাজার দর");
        bn.put("price_trend", "৭ দিনের দরের গতিপ্রকৃতি");
        bn.put("weather_impact", "আবহাওয়ার প্রভাব");
        bn.put("farmer_advice", "কৃষক পরামর্শ");
        bn.put("sort_by", "সাজান");
        bn.put("date_range", "তারিখের পরিসীমা");
        bn.put("production_costs", "১. উৎপাদন খরচ");
        bn.put("expected_harvest", "২. প্রত্যাশিত ফলন");
        bn.put("financial_analysis", "৩. আর্থিক বিশ্লেষণ");
        bn.put("calculate_financials", "লাভ-ক্ষতি হিসাব করুন");
        bn.put("estimate_yield", "ফলন অনুমান করুন");
        bn.put("generate_plan", "সেচ পরিকল্পনা তৈরি করুন");
        bn.put("health_status", "স্বাস্থ্য অবস্থা");
        bn.put("survival_chance", "বেঁচে থাকার সম্ভাবনা");
        bn.put("summary", "সারসংক্ষেপ");
        bn.put("recommended_actions", "করণীয় (Do's)");
        bn.put("things_to_avoid", "বর্জনীয় (Don'ts)");
        bn.put("cures_fertilizers", "প্রতিকার ও সার");
        bn.put("apply_to_tools", "সম্পর্কিত কৃষি সরঞ্জাম");
        bn.put("fertilizer_calc", "সার ক্যালকুলেটর");
        bn.put("cost_calc", "খরচ ক্যালকুলেটর");
        bn.put("irrigation_plan", "সেচ পরিকল্পনা");
        bn.put("detect_location", "অবস্থান সনাক্ত করুন");
        bn.put("weather_title", "আবহাওয়া তথ্য");
        bn.put("how_to_use", "ব্যবহার নির্দেশিকা");
        bn.put("about_developer", "ডেভেলপার পরিচিতি");
        bn.put("general_variety", "সাধারণ");
        bn.put("other_variety", "অন্যান্য");
        bn.put("alluvial_soil", "পলি মাটি");
        bn.put("black_soil", "কালো মাটি");
        bn.put("red_soil", "লাল মাটি");
        bn.put("laterite_soil", "ল্যাটেরাইট");
        bn.put("sandy_soil", "বেলে মাটি");
        bn.put("clay_soil", "এঁটেল মাটি");
        bn.put("flood_irrigation", "প্লাবন সেচ");
        bn.put("drip_irrigation", "ড্রিপ সেচ");
        bn.put("sprinkler_irrigation", "স্প্রিংকলার");
        bn.put("furrow_irrigation", "নালা সেচ");
        bn.put("basin_irrigation", "বেসিন সেচ");
        bn.put("crop_wheat", "গম");
        bn.put("crop_rice", "চাল");
        bn.put("crop_maize", "ভুট্টা");
        bn.put("crop_cotton", "তুলা");
        bn.put("crop_sugarcane", "আখ");
        bn.put("crop_soybean", "সয়াবিন");
        bn.put("crop_mustard", "সরিষা");
        bn.put("crop_potato", "আলু");
        bn.put("crop_onion", "পেঁয়াজ");
        bn.put("crop_chili", "লঙ্কা");
        bn.put("crop_tomato", "টমেটো");
        bn.put("crop_paddy", "ধান");
        bn.put("crop_millets", "বাজরা");
        bn.put("crop_pulses", "ডাল");
        bn.put("acre", "একর");
        bn.put("hectare", "হেক্টর");
        bn.put("bigha", "বিঘা");
        bn.put("kanal", "কানাল");
        bn.put("biswa", "বিসওয়া");
        bn.put("cent", "সেন্ট");
        bn.put("guntha", "গুন্ঠা");
        bn.put("katha", "কাঠা");
        bn.put("decimal", "ডেসিমেল");
        bn.put("find_centers", "কৃষি কেন্দ্র খুঁজুন");
        bn.put("healthy", "সুস্থ");
        bn.put("detected", "সনাক্ত করা রোগ");
        bn.put("click_for_info", "আরও তথ্যের জন্য ক্লিক করুন");
        bn.put("response_time", "প্রতিক্রিয়া সময়: %.1f সেকেন্ড");
        bn.put("note_optional_date", "নোট: তারিখ নির্বাচন ঐচ্ছিক।");
        bn.put("order", "ক্রম");
        bn.put("avg_yield", "গড় ফলন");
        bn.put("quintals", "কুইন্টাল");
        bn.put("quintals_acre", "কুইন্টাল/একর");
        bn.put("irrigation_interval", "সেচের ব্যবধান");
        bn.put("days", "দিন");
        bn.put("unit", "একক");
        bn.put("area", "পরিমাপ");
        bn.put("calculate_requirements", "প্রয়োজনীয়তা গণনা করুন");
        DICTIONARY.put("bn", bn);

        Map<String, String> te = new HashMap<>();
        te.put("jai_kisan", "జై కిసాన్");
        te.put("app_motto", "నా మట్టి, నా రైతు");
        te.put("financial_pro_report", "ఫైనాన్షియల్ ప్రో రిపోర్ట్");
        te.put("crop", "పంట");
        te.put("state", "రాష్ట్రం");
        te.put("city", "జిల్లా / మార్కెట్");
        te.put("variety", "రకం");
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
        te.put("loss", "నష్టం");
        te.put("profit_margin", "లాభం శాతం");
        te.put("break_even", "బ్రేక్-ఈవెన్ ధర");
        te.put("good_crop_choice", "మంచి పంట ఎంపిక");
        te.put("medium_risk", "మధ్యస్థ ప్రమాదం");
        te.put("warning_risk", "హెచ్చరిక");
        te.put("safe_harvest_tip", "సురక్షితమైన దిగుబడి కానీ ఖర్చులను గమనించండి.");
        te.put("low_margin_tip", "లాభం చాలా తక్కువ. అధిక ప్రమాదం ఉంది.");
        te.put("excellent_margin_tip", "మీ పెట్టుబడి ఆధారంగా అద్భుతమైన లాభ అవకాశం ఉంది.");
        te.put("land_area", "భూమి విస్తీర్ణం");
        te.put("recommended_fertilizer", "సిఫార్సు చేసిన ఎరువులు");
        te.put("shopping_list", "కొనుగోలు జాబితా");
        te.put("liquid_option", "ద్రవ ప్రత్యామ్నాయం (Nano Urea)");
        te.put("ai_advisory", "AI నిపుణుల సలహా");
        te.put("urea", "యూరియా");
        te.put("dap", "డీఏపీ");
        te.put("mop", "ఎంవోపీ");
        te.put("nitrogen", "నైట్రోజన్");
        te.put("phosphorus", "భాస్వరం");
        te.put("potassium", "పొటాషియం");
        te.put("soil_type", "నేల రకం");
        te.put("irrigation_method", "సాగునీటి పద్ధతి");
        te.put("critical_stage", "కీలక దశ");
        te.put("expert_tip", "నిపుణుల చిట్కా");
        te.put("irrigation_report", "నీటిపారుదల ప్రణాళిక నివేదిక");
        te.put("yield_report", "దిగుబడి అంచనా నివేదిక");
        te.put("mandi_report", "మార్కెట్ ధరల నివేదిక");
        te.put("diagnosis_report", "పంట ఆరోగ్య నివేదిక");
        te.put("voice_report", "వాయిస్ అసిస్టెంట్ నివేదిక");
        te.put("share_report", "నివేదికను పంచుకోండి");
        te.put("share_text", "టెక్స్ట్‌గా పంచుకోండి");
        te.put("share_pdf", "పిడిఎఫ్‌గా పంచుకోండి");
        te.put("share_option", "షేర్ ఎంపిక");
        te.put("share_text_desc", "నివేదికను సందేశంగా పంచుకోండి.");
        te.put("share_pdf_desc", "అందమైన పిడిఎఫ్ పత్రాన్ని పంచుకోండి.");
        te.put("cancel", "రద్దు చేయండి");
        te.put("settings", "సెట్టింగులు");
        te.put("language", "భాష");
        te.put("photo_expert", "ఫోటో నిపుణుడు");
        te.put("detect_desc", "ఏదైనా పంట, ఆకు లేదా పండ్లను విశ్లేషించండి. నిపుణుల నివేదిక పొందడానికి ఫోటో తీయండి లేదా అప్‌లోడ్ చేయండి.");
        te.put("no_image_selected", "చిత్రం ఎంపిక చేయలేదు");
        te.put("capture", "ఫోటో తీయండి");
        te.put("upload", "అప్‌లోడ్ చేయండి");
        te.put("analyze_photo", "ఫోటోను విశ్లేషించండి");
        te.put("date", "తేదీ");
        te.put("generated_via", "జై కిసాన్ యాప్ ద్వారా రూపొందించబడింది • భారతీయ వ్యవసాయానికి తోడ్పాటు");
        te.put("home_detect_disease", "తెగుళ్ల గుర్తింపు");
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
        te.put("voice_assistant", "వాయిస్ అసిస్టెంట్");
        te.put("tap_to_ask", "అడగడానికి నొక్కండి");
        te.put("listening", "వింటున్నాను...");
        te.put("thinking", "ఆలోచిస్తున్నాను...");
        te.put("voice_greeting", "నమస్కారం! మీ వ్యవసాయ సహాయం కోసం నేను ఇక్కడ ఉన్నాను.");
        te.put("mandi_prices", "మార్కెట్ ధరలు");
        te.put("select_crop", "పంటను ఎంచుకోండి");
        te.put("select_state", "రాష్ట్రం ఎంచుకోండి");
        te.put("select_city", "జిల్లా / మార్కెట్ ఎంచుకోండి");
        te.put("select_variety", "రకాన్ని ఎంచుకోండి");
        te.put("select_date", "తేదీ ఎంచుకోండి");
        te.put("get_prices", "ధరలు పొందండి");
        te.put("market_insight", "మార్కెట్ విశ్లేషణ");
        te.put("price_range", "ధర పరిధి");
        te.put("modal_price", "సగటు ధర");
        te.put("price_trend", "7-రోజుల ధరల సరళి");
        te.put("weather_impact", "వాతావరణ ప్రభావం");
        te.put("farmer_advice", "రైతు సలహా");
        te.put("sort_by", "క్రమబద్ధీకరించు");
        te.put("date_range", "తేదీ పరిధి");
        te.put("production_costs", "1. ఉత్పత్తి ఖర్చులు");
        te.put("expected_harvest", "2. ఆశించిన దిగుబడి");
        te.put("financial_analysis", "3. ఆర్థిక విశ్లేషణ");
        te.put("calculate_financials", "ఆదాయ వ్యయాలు లెక్కించండి");
        te.put("estimate_yield", "దిగుబడిని అంచనా వేయండి");
        te.put("generate_plan", "నీటిపారుదల ప్రణాళిక రూపొందించండి");
        te.put("health_status", "ఆరోగ్య స్థితి");
        te.put("survival_chance", "బతికే అవకాశం");
        te.put("summary", "సారాంశం");
        te.put("recommended_actions", "చేయవలసినవి (Do's)");
        te.put("things_to_avoid", "చేయకూడనివి (Don'ts)");
        te.put("cures_fertilizers", "నివారణ మరియు ఎరువులు");
        te.put("apply_to_tools", "సంబంధిత వ్యవసాయ సాధనాలు");
        te.put("fertilizer_calc", "ఎరువుల లెక్కింపు");
        te.put("cost_calc", "ఖర్చు లెక్కింపు");
        te.put("irrigation_plan", "నీటి ప్రణాళిక");
        te.put("detect_location", "లొకేషన్ గుర్తించండి");
        te.put("weather_title", "వాతావరణ సమాచారం");
        te.put("how_to_use", "వినియోగ విధానం");
        te.put("about_developer", "డెవలపర్ వివరాలు");
        te.put("general_variety", "సాధారణ");
        te.put("other_variety", "ఇతర");
        te.put("alluvial_soil", "ఒండ్రు నేల");
        te.put("black_soil", "నల్లరేగడి నేల");
        te.put("red_soil", "ఎర్ర నేల");
        te.put("laterite_soil", "లేటరైట్ నేల");
        te.put("sandy_soil", "ఇసుక నేల");
        te.put("clay_soil", "బంకమట్టి నేల");
        te.put("flood_irrigation", "కాలువ నీరు");
        te.put("drip_irrigation", "బిందు సేద్యం (డ్రిప్)");
        te.put("sprinkler_irrigation", "తుంపర సేద్యం");
        te.put("furrow_irrigation", "సాలు పద్ధతి");
        te.put("basin_irrigation", "పాదు పద్ధతి");
        te.put("crop_wheat", "గోధుమ");
        te.put("crop_rice", "బియ్యం");
        te.put("crop_maize", "మొక్కజొన్న");
        te.put("crop_cotton", "పత్తి");
        te.put("crop_sugarcane", "చెరకు");
        te.put("crop_soybean", "సోయాబీన్");
        te.put("crop_mustard", "ఆవాలు");
        te.put("crop_potato", "బంగాళాదుంప");
        te.put("crop_onion", "ఉల్లిపాయ");
        te.put("crop_chili", "మిరపకాయ");
        te.put("crop_tomato", "టమోటా");
        te.put("crop_paddy", "వరి");
        te.put("crop_millets", "సజ్జలు");
        te.put("crop_pulses", "పప్పుధాన్యాలు");
        te.put("acre", "ఎకరం");
        te.put("hectare", "హెక్టారు");
        te.put("bigha", "బిఘా");
        te.put("kanal", "కెనాల్");
        te.put("biswa", "బిస్వా");
        te.put("cent", "సెంట్");
        te.put("guntha", "గుంట");
        te.put("katha", "కథా");
        te.put("decimal", "డెసిమల్");
        te.put("find_centers", "వ్యవసాయ కేంద్రాలను కనుగొనండి");
        te.put("healthy", "ఆరోగ్యకరమైనది");
        te.put("detected", "గుర్తించిన వ్యాధి");
        te.put("click_for_info", "మరింత సమాచారం కోసం క్లిక్ చేయండి");
        te.put("response_time", "ప్రతిస్పందన సమయం: %.1f సెకన్లు");
        te.put("note_optional_date", "గమనిక: తేదీ ఎంపిక ఐచ్ఛికం.");
        te.put("order", "క్రమం");
        te.put("avg_yield", "సగటు దిగుబడి");
        te.put("quintals", "క్వింటాళ్ళు");
        te.put("quintals_acre", "క్వింటాళ్ళు/ఎకరం");
        te.put("irrigation_interval", "నీటిపారుదల విరామం");
        te.put("days", "రోజులు");
        te.put("unit", "యూనిట్");
        te.put("area", "వైశాల్యం");
        te.put("calculate_requirements", "ఎరువుల అవసరాలు లెక్కించండి");
        DICTIONARY.put("te", te);

        Map<String, String> mr = new HashMap<>();
        mr.put("jai_kisan", "जय किसान");
        mr.put("app_motto", "माझी माती, माझे शेतकरी");
        mr.put("financial_pro_report", "आर्थिक प्रो अहवाल");
        mr.put("crop", "पीक");
        mr.put("state", "राज्य");
        mr.put("city", "जिल्हा / बाजार");
        mr.put("variety", "वाण");
        mr.put("total_cost", "एकूण खर्च");
        mr.put("cost_breakdown", "खर्चाचा तपशील");
        mr.put("harvest_estimation", "उत्पादन आणि महसूल");
        mr.put("seed", "बियाणे");
        mr.put("fertilizer", "खते");
        mr.put("labor", "मजूर");
        mr.put("other", "इतर");
        mr.put("total_yield", "एकूण उत्पादन");
        mr.put("price", "भाव");
        mr.put("total_revenue", "एकूण महसूल");
        mr.put("net_profit", "निव्वळ नफा");
        mr.put("loss", "तोटा");
        mr.put("profit_margin", "नफा मार्जिन");
        mr.put("break_even", "नफा-तोटा समतोल भाव");
        mr.put("good_crop_choice", "उत्तम पीक निवड");
        mr.put("medium_risk", "मध्यम जोखीम");
        mr.put("warning_risk", "धोका / सूचना");
        mr.put("safe_harvest_tip", "उत्पादन सुरक्षित आहे पण खर्चावर लक्ष ठेवा.");
        mr.put("low_margin_tip", "नफा मार्जिन खूप कमी आहे. जोखीम जास्त आहे.");
        mr.put("excellent_margin_tip", "तुमच्या इनपुटनुसार उत्कृष्ट नफ्याची शक्यता आहे.");
        mr.put("land_area", "जमिनीचे क्षेत्रफळ");
        mr.put("recommended_fertilizer", "शिफारस केलेली खते");
        mr.put("shopping_list", "खरेदी यादी");
        mr.put("liquid_option", "द्रवरूप पर्याय (Nano Urea)");
        mr.put("ai_advisory", "AI तज्ज्ञ सल्ला");
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
        mr.put("irrigation_report", "सिंचन नियोजन अहवाल");
        mr.put("yield_report", "उत्पादन अंदाज अहवाल");
        mr.put("mandi_report", "बाजार भाव अहवाल");
        mr.put("diagnosis_report", "पीक आरोग्य अहवाल");
        mr.put("voice_report", "व्हॉइस असिस्टंट अहवाल");
        mr.put("share_report", "अहवाल शेअर करा");
        mr.put("share_text", "मजकूर म्हणून शेअर करा");
        mr.put("share_pdf", "पीडीएफ शेअर करा");
        mr.put("share_option", "शेअर पर्याय");
        mr.put("share_text_desc", "अहवाल संदेश म्हणून शेअर करा.");
        mr.put("share_pdf_desc", "एक सुंदर पीडीएफ दस्तऐवज शेअर करा.");
        mr.put("cancel", "रद्द करा");
        mr.put("settings", "सेटिंग्ज");
        mr.put("language", "भाषा");
        mr.put("photo_expert", "फोटो तज्ज्ञ");
        mr.put("detect_desc", "कोणत्याही पिकाचे, पानांचे किंवा फळांचे विश्लेषण करा. तज्ज्ञ अहवाल मिळवण्यासाठी फोटो काढा किंवा अपलोड करा.");
        mr.put("no_image_selected", "कोणतीही प्रतिमा निवडलेली नाही");
        mr.put("capture", "फोटो काढा");
        mr.put("upload", "अपलोड करा");
        mr.put("analyze_photo", "फोटोचे विश्लेषण करा");
        mr.put("date", "दिनांक");
        mr.put("generated_via", "जय किसान ॲपद्वारे निर्मित • भारतीय शेतीला पाठबळ");
        mr.put("home_detect_disease", "रोग निदान");
        mr.put("home_voice_assistant", "व्हॉइस सहाय्यक");
        mr.put("home_mandi_prices", "बाजार भाव");
        mr.put("home_how_to_use", "कसे वापरावे");
        mr.put("home_weather_info", "हवामान माहिती");
        mr.put("home_farming_tools", "शेती अवजारे");
        mr.put("home_about", "माहिती");
        mr.put("fertilizer_calculator", "खत कॅल्क्युलेटर");
        mr.put("yield_estimator", "उत्पादन अंदाज");
        mr.put("irrigation_planner", "सिंचन नियोजन");
        mr.put("cost_calculator", "खर्च कॅल्क्युलेटर");
        mr.put("voice_assistant", "व्हॉइस सहाय्यक");
        mr.put("tap_to_ask", "विचारण्यासाठी टॅप करा");
        mr.put("listening", "ऐकत आहे...");
        mr.put("thinking", "विचार करत आहे...");
        mr.put("voice_greeting", "नमस्कार! मी आपल्या कृषी मदतीसाठी येथे आहे.");
        mr.put("mandi_prices", "बाजार भाव");
        mr.put("select_crop", "पीक निवडा");
        mr.put("select_state", "राज्य निवडा");
        mr.put("select_city", "जिल्हा / बाजार निवडा");
        mr.put("select_variety", "वाण निवडा");
        mr.put("select_date", "तारीख निवडा");
        mr.put("get_prices", "बाजार भाव मिळवा");
        mr.put("market_insight", "बाजार विश्लेषण");
        mr.put("price_range", "किंमत श्रेणी");
        mr.put("modal_price", "सरासरी भाव");
        mr.put("price_trend", "७ दिवसांचा भाव कल");
        mr.put("weather_impact", "हवामानाचा प्रभाव");
        mr.put("farmer_advice", "शेतकरी सल्ला");
        mr.put("sort_by", "क्रमवारी लावा");
        mr.put("date_range", "तारीख श्रेणी");
        mr.put("production_costs", "१. उत्पादन खर्च");
        mr.put("expected_harvest", "२. अपेक्षित उत्पादन");
        mr.put("financial_analysis", "३. आर्थिक विश्लेषण");
        mr.put("calculate_financials", "नफा-तोटा गणना करा");
        mr.put("estimate_yield", "उत्पादनाचा अंदाज घ्या");
        mr.put("generate_plan", "सिंचन योजना तयार करा");
        mr.put("health_status", "आरोग्य स्थिती");
        mr.put("survival_chance", "वाचण्याची शक्यता");
        mr.put("summary", "सारांश");
        mr.put("recommended_actions", "काय करावे (Do's)");
        mr.put("things_to_avoid", "काय करू नये (Don'ts)");
        mr.put("cures_fertilizers", "उपाय आणि खते");
        mr.put("apply_to_tools", "संबंधित शेती साधने");
        mr.put("fertilizer_calc", "खत मोजणी");
        mr.put("cost_calc", "खर्च मोजणी");
        mr.put("irrigation_plan", "सिंचन योजना");
        mr.put("detect_location", "स्थान शोधा");
        mr.put("weather_title", "हवामान अंदाज");
        mr.put("how_to_use", "वापर मार्गदर्शक");
        mr.put("about_developer", "डेव्हलपर माहिती");
        mr.put("general_variety", "सामान्य");
        mr.put("other_variety", "इतर");
        mr.put("alluvial_soil", "गाळाची माती");
        mr.put("black_soil", "काळी माती");
        mr.put("red_soil", "तांबडी माती");
        mr.put("laterite_soil", "जांभी माती");
        mr.put("sandy_soil", "रेताड माती");
        mr.put("clay_soil", "चिकण माती");
        mr.put("flood_irrigation", "मोकाट सिंचन");
        mr.put("drip_irrigation", "ठिबक सिंचन");
        mr.put("sprinkler_irrigation", "तुषार सिंचन");
        mr.put("furrow_irrigation", "सरी सिंचन");
        mr.put("basin_irrigation", "आळे सिंचन");
        mr.put("crop_wheat", "गहू");
        mr.put("crop_rice", "तांदूळ");
        mr.put("crop_maize", "मका");
        mr.put("crop_cotton", "कापूस");
        mr.put("crop_sugarcane", "ऊस");
        mr.put("crop_soybean", "सोयाबीन");
        mr.put("crop_mustard", "मोहरी");
        mr.put("crop_potato", "बटाटा");
        mr.put("crop_onion", "कांदा");
        mr.put("crop_chili", "मिरची");
        mr.put("crop_tomato", "टोमॅटो");
        mr.put("crop_paddy", "धान");
        mr.put("crop_millets", "बाजरी");
        mr.put("crop_pulses", "डाळी");
        mr.put("acre", "एकर");
        mr.put("hectare", "हेक्टर");
        mr.put("bigha", "बिघा");
        mr.put("kanal", "कनाल");
        mr.put("biswa", "बिस्वा");
        mr.put("cent", "सेंट");
        mr.put("guntha", "गुंठा");
        mr.put("katha", "कट्ठा");
        mr.put("decimal", "डेसीमल");
        mr.put("find_centers", "कृषी केंद्र शोधा");
        mr.put("healthy", "निरोगी");
        mr.put("detected", "रोग आढळला");
        mr.put("click_for_info", "अधिक माहितीसाठी येथे क्लिक करा");
        mr.put("response_time", "प्रतिसाद वेळ: %.1f सेकंद");
        mr.put("note_optional_date", "टीप: तारीख निवड पर्यायी आहे.");
        mr.put("order", "क्रम");
        mr.put("avg_yield", "सरासरी उत्पादन");
        mr.put("quintals", "क्विंटल");
        mr.put("quintals_acre", "क्विंटल/एकर");
        mr.put("irrigation_interval", "पाणी देण्याचा कालावधी");
        mr.put("days", "दिवस");
        mr.put("unit", "एकक");
        mr.put("area", "क्षेत्रफळ");
        mr.put("calculate_requirements", "गरज मोजा");
        DICTIONARY.put("mr", mr);

        Map<String, String> ta = new HashMap<>();
        ta.put("jai_kisan", "ஜெய் கிசான்");
        ta.put("app_motto", "என் மண், என் உழவர்");
        ta.put("financial_pro_report", "நிதி புரோ அறிக்கை");
        ta.put("crop", "பயிர்");
        ta.put("state", "மாநிலம்");
        ta.put("city", "மாவட்டம் / சந்தை");
        ta.put("variety", "ரகம்");
        ta.put("total_cost", "மொத்த செலவு");
        ta.put("cost_breakdown", "செலவு விவரம்");
        ta.put("harvest_estimation", "மகசூல் மற்றும் வருவாய்");
        ta.put("seed", "விதை");
        ta.put("fertilizer", "உரம்");
        ta.put("labor", "கூலி");
        ta.put("other", "மற்றவை");
        ta.put("total_yield", "மொத்த மகசூல்");
        ta.put("price", "விலை");
        ta.put("total_revenue", "மொத்த வருமானம்");
        ta.put("net_profit", "நிகர லாபம்");
        ta.put("loss", "நஷ்டம்");
        ta.put("profit_margin", "லாப வரம்பு");
        ta.put("break_even", "சமநிலை விலை");
        ta.put("good_crop_choice", "சிறந்த பயிர் தேர்வு");
        ta.put("medium_risk", "நடுத்தர ஆபத்து");
        ta.put("warning_risk", "எச்சரிக்கை");
        ta.put("safe_harvest_tip", "பாதுகாப்பான அறுவடை, ஆனால் செலவுகளைக் கண்காணிக்கவும்.");
        ta.put("low_margin_tip", "லாப வரம்பு மிகக் குறைவு. அதிக ஆபத்து உள்ளது.");
        ta.put("excellent_margin_tip", "உங்கள் உள்ளீட்டின் அடிப்படையில் சிறந்த லாப வாய்ப்பு உள்ளது.");
        ta.put("land_area", "நிலப்பரப்பு");
        ta.put("recommended_fertilizer", "பரிந்துரைக்கப்பட்ட உரம்");
        ta.put("shopping_list", "வாங்கும் பட்டியல்");
        ta.put("liquid_option", "திரவ மாற்று (Nano Urea)");
        ta.put("ai_advisory", "AI நிபுணர் ஆலோசனை");
        ta.put("urea", "யூரியா");
        ta.put("dap", "டிஏபி");
        ta.put("mop", "எம்ஓபி");
        ta.put("nitrogen", "நைட்ரஜன்");
        ta.put("phosphorus", "பாஸ்பரஸ்");
        ta.put("potassium", "பொட்டாசியம்");
        ta.put("soil_type", "மண் வகை");
        ta.put("irrigation_method", "பாசன முறை");
        ta.put("critical_stage", "முக்கிய நிலை");
        ta.put("expert_tip", "நிபுணர் குறிப்பு");
        ta.put("irrigation_report", "பாசன திட்ட அறிக்கை");
        ta.put("yield_report", "மகசூல் மதிப்பீட்டு அறிக்கை");
        ta.put("mandi_report", "சந்தை விலை அறிக்கை");
        ta.put("diagnosis_report", "பயிர் சுகாதார அறிக்கை");
        ta.put("voice_report", "குரல் உதவியாளர் அறிக்கை");
        ta.put("share_report", "அறிக்கையைப் பகிரவும்");
        ta.put("share_text", "உரையாகப் பகிரவும்");
        ta.put("share_pdf", "PDF பகிரவும்");
        ta.put("share_option", "பகிர்வு விருப்பம்");
        ta.put("share_text_desc", "அறிக்கையை செய்தியாகப் பகிரவும்.");
        ta.put("share_pdf_desc", "அழகான PDF ஆவணத்தைப் பகிரவும்.");
        ta.put("cancel", "ரத்து செய்");
        ta.put("settings", "அமைப்புகள்");
        ta.put("language", "மொழி");
        ta.put("photo_expert", "புகைப்பட நிபுணர்");
        ta.put("detect_desc", "ஏதேனும் பயிர், இலை அல்லது பழத்தை பகுப்பாய்வு செய்யுங்கள். நிபுணர் அறிக்கையைப் பெற புகைப்படம் எடுக்கவும் அல்லது பதிவேற்றவும்.");
        ta.put("no_image_selected", "படம் தேர்ந்தெடுக்கப்படவில்லை");
        ta.put("capture", "படம் எடு");
        ta.put("upload", "பதிவேற்றவும்");
        ta.put("analyze_photo", "புகைப்படத்தை பகுப்பாய்வு செய்");
        ta.put("date", "தேதி");
        ta.put("generated_via", "ஜெய் கிசான் செயலி மூலம் உருவாக்கப்பட்டது • இந்திய விவசாயத்திற்கு துணை");
        ta.put("home_detect_disease", "நோய் கண்டறிதல்");
        ta.put("home_voice_assistant", "குரல் உதவியாளர்");
        ta.put("home_mandi_prices", "சந்தை விலைகள்");
        ta.put("home_how_to_use", "எப்படி உபயோகிப்பது");
        ta.put("home_weather_info", "வானிலை தகவல்");
        ta.put("home_farming_tools", "விவசாயக் கருவிகள்");
        ta.put("home_about", "பற்றி");
        ta.put("fertilizer_calculator", "உரக் கால்குலேட்டர்");
        ta.put("yield_estimator", "மகசூல் மதிப்பீட்டாளர்");
        ta.put("irrigation_planner", "பாசன திட்டமிடுபவர்");
        ta.put("cost_calculator", "செலவு கால்குலேட்டர்");
        ta.put("voice_assistant", "குரல் உதவியாளர்");
        ta.put("tap_to_ask", "கேட்க தட்டவும்");
        ta.put("listening", "கேட்கிறது...");
        ta.put("thinking", "யோசிக்கிறது...");
        ta.put("voice_greeting", "வணக்கம்! உங்கள் விவசாய உதவிக்காக நான் இங்கே இருக்கிறேன்.");
        ta.put("mandi_prices", "சந்தை விலைகள்");
        ta.put("select_crop", "பயிரைத் தேர்ந்தெடுக்கவும்");
        ta.put("select_state", "மாநிலத்தைத் தேர்ந்தெடுக்கவும்");
        ta.put("select_city", "மாவட்டம் / சந்தையைத் தேர்ந்தெடுக்கவும்");
        ta.put("select_variety", "ரகத்தைத் தேர்ந்தெடுக்கவும்");
        ta.put("select_date", "தேதியைத் தேர்ந்தெடுக்கவும்");
        ta.put("get_prices", "சந்தை விலைகளைப் பெறுங்கள்");
        ta.put("market_insight", "சந்தை நுண்ணறிவு");
        ta.put("price_range", "விலை வரம்பு");
        ta.put("modal_price", "சராசரி விலை");
        ta.put("price_trend", "7-நாள் விலை போக்கு");
        ta.put("weather_impact", "வானிலை தாக்கம்");
        ta.put("farmer_advice", "விவசாயி ஆலோசனை");
        ta.put("sort_by", "வரிசைப்படுத்து");
        ta.put("date_range", "தேதி வரம்பு");
        ta.put("production_costs", "1. உற்பத்தி செலவுகள்");
        ta.put("expected_harvest", "2. எதிர்பார்க்கப்படும் அறுவடை");
        ta.put("financial_analysis", "3. நிதி பகுப்பாய்வு");
        ta.put("calculate_financials", "நிதி கணக்கீடு செய்");
        ta.put("estimate_yield", "மகசூலை மதிப்பிடுங்கள்");
        ta.put("generate_plan", "பாசனத் திட்டத்தை உருவாக்குங்கள்");
        ta.put("health_status", "சுகாதார நிலை");
        ta.put("survival_chance", "உயிர்வாழும் வாய்ப்பு");
        ta.put("summary", "சுருக்கம்");
        ta.put("recommended_actions", "செய்ய வேண்டியவை (Do's)");
        ta.put("things_to_avoid", "தவிர்க்க வேண்டியவை (Don'ts)");
        ta.put("cures_fertilizers", "சிகிச்சைகள் & உரங்கள்");
        ta.put("apply_to_tools", "தொடர்புடைய விவசாயக் கருவிகள்");
        ta.put("fertilizer_calc", "உரக் கணக்கீடு");
        ta.put("cost_calc", "செலவு கணக்கீடு");
        ta.put("irrigation_plan", "பாசனத் திட்டம்");
        ta.put("detect_location", "இருப்பிடத்தைக் கண்டறி");
        ta.put("weather_title", "வானிலை அறிக்கை");
        ta.put("how_to_use", "பயன்பாட்டு வழிகாட்டி");
        ta.put("about_developer", "உருவாக்குநர் பற்றி");
        ta.put("general_variety", "பொதுவானது");
        ta.put("other_variety", "மற்றவை");
        ta.put("alluvial_soil", "வண்டல் மண்");
        ta.put("black_soil", "கரிசல் மண்");
        ta.put("red_soil", "செம்மண்");
        ta.put("laterite_soil", "சரளை மண்");
        ta.put("sandy_soil", "மணல் மண்");
        ta.put("clay_soil", "களிமண்");
        ta.put("flood_irrigation", "வாய்க்கால் பாசனம்");
        ta.put("drip_irrigation", "சொட்டு நீர் பாசனம்");
        ta.put("sprinkler_irrigation", "தெளிப்பு நீர் பாசனம்");
        ta.put("furrow_irrigation", "பார் பாசனம்");
        ta.put("basin_irrigation", "வளையப் பாசனம்");
        ta.put("crop_wheat", "கோதுமை");
        ta.put("crop_rice", "அரிசி");
        ta.put("crop_maize", "மக்காச்சோளம்");
        ta.put("crop_cotton", "பருத்தி");
        ta.put("crop_sugarcane", "கரும்பு");
        ta.put("crop_soybean", "சோயாபீன்");
        ta.put("crop_mustard", "கடுகு");
        ta.put("crop_potato", "உருளைக்கிழங்கு");
        ta.put("crop_onion", "வெங்காயம்");
        ta.put("crop_chili", "மிளகாய்");
        ta.put("crop_tomato", "தக்காளி");
        ta.put("crop_paddy", "நெல்");
        ta.put("crop_millets", "கம்பு");
        ta.put("crop_pulses", "பருப்பு வகைகள்");
        ta.put("acre", "ஏக்கர்");
        ta.put("hectare", "ஹெக்டேர்");
        ta.put("bigha", "பிகா");
        ta.put("kanal", "கனால்");
        ta.put("biswa", "பிஸ்வா");
        ta.put("cent", "சென்ட்");
        ta.put("guntha", "குந்தா");
        ta.put("katha", "கத்தா");
        ta.put("decimal", "டெசிமல்");
        ta.put("find_centers", "வேளாண் மையங்களைக் கண்டறியவும்");
        ta.put("healthy", "ஆரோக்கியமானது");
        ta.put("detected", "கண்டறியப்பட்டது");
        ta.put("click_for_info", "மேலும் தகவலுக்கு கிளிக் செய்யவும்");
        ta.put("response_time", "பதில் நேரம்: %.1f வினாடிகள்");
        ta.put("note_optional_date", "குறிப்பு: தேதி தேர்வு விருப்பமானது.");
        ta.put("order", "வரிசை");
        ta.put("avg_yield", "சராசரி மகசூல்");
        ta.put("quintals", "குவிண்டால்");
        ta.put("quintals_acre", "குவிண்டால்/ஏக்கர்");
        ta.put("irrigation_interval", "பாசன இடைவெளி");
        ta.put("days", "நாட்கள்");
        ta.put("unit", "அலகு");
        ta.put("area", "பரப்பளவு");
        ta.put("calculate_requirements", "உரத் தேவையை கணக்கிடுங்கள்");
        DICTIONARY.put("ta", ta);

        Map<String, String> gu = new HashMap<>();
        gu.put("jai_kisan", "જય કિસાન");
        gu.put("app_motto", "મારી માટી, મારા ખેડૂત");
        gu.put("financial_pro_report", "નાણાકીય પ્રો રિપોર્ટ");
        gu.put("crop", "પાક");
        gu.put("state", "રાજ્ય");
        gu.put("city", "જિલ્લો / માર્કેટ");
        gu.put("variety", "જાત");
        gu.put("total_cost", "કુલ ખર્ચ");
        gu.put("cost_breakdown", "ખર્ચની વિગતો");
        gu.put("harvest_estimation", "ઉપજ અને આવક");
        gu.put("seed", "બીજ");
        gu.put("fertilizer", "ખાતર");
        gu.put("labor", "મજૂર");
        gu.put("other", "અન્ય");
        gu.put("total_yield", "કુલ ઉપજ");
        gu.put("price", "ભાવ");
        gu.put("total_revenue", "કુલ આવક");
        gu.put("net_profit", "ચોખ્ખો નફો");
        gu.put("loss", "નુકસાન");
        gu.put("profit_margin", "નફાનું પ્રમાણ");
        gu.put("break_even", "બ્રેક-ઇવન ભાવ");
        gu.put("good_crop_choice", "ઉત્તમ પાકની પસંદગી");
        gu.put("medium_risk", "મધ્યમ જોખમ");
        gu.put("warning_risk", "ચેતવણી");
        gu.put("safe_harvest_tip", "સારી ઉપજ છે પરંતુ ખર્ચ પર નજર રાખો.");
        gu.put("low_margin_tip", "નફો ઘણો ઓછો છે. જોખમ વધારે છે.");
        gu.put("excellent_margin_tip", "તમારા રોકાણ મુજબ ઉત્તમ નફાની સંભાવના છે.");
        gu.put("land_area", "જમીનનો વિસ્તાર");
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
        gu.put("soil_type", "જમીનનો પ્રકાર");
        gu.put("irrigation_method", "પિયત પદ્ધતિ");
        gu.put("critical_stage", "મહત્વપૂર્ણ તબક્કો");
        gu.put("expert_tip", "નિષ્ણાત સલાહ");
        gu.put("irrigation_report", "પિયત આયોજન રિપોર્ટ");
        gu.put("yield_report", "ઉપજ અંદાજ રિપોર્ટ");
        gu.put("mandi_report", "બજાર ભાવ રિપોર્ટ");
        gu.put("diagnosis_report", "પાક આરોગ્ય રિપોર્ટ");
        gu.put("voice_report", "વોઇસ આસિસ્ટન્ટ રિપોર્ટ");
        gu.put("share_report", "રિપોર્ટ શેર કરો");
        gu.put("share_text", "ટેક્સ્ટ તરીકે શેર કરો");
        gu.put("share_pdf", "પીડીએફ શેર કરો");
        gu.put("share_option", "શેર વિકલ્પ");
        gu.put("share_text_desc", "અહેવાલ સંદેશ તરીકે શેર કરો.");
        gu.put("share_pdf_desc", "એક સુંદર પીડીએફ દસ્તાવેજ શેર કરો.");
        gu.put("cancel", "રદ કરો");
        gu.put("settings", "સેટિંગ્સ");
        gu.put("language", "ભાષા");
        gu.put("photo_expert", "ફોટો નિષ્ણાત");
        gu.put("detect_desc", "કોઈપણ પાક, પાન અથવા ફળનું વિશ્લેષણ કરો. નિષ્ણાત રિપોર્ટ મેળવવા માટે ફોટો પાડો અથવા અપલોડ કરો.");
        gu.put("no_image_selected", "કોઈ ચિત્ર પસંદ કરેલ નથી");
        gu.put("capture", "ફોટો લો");
        gu.put("upload", "અપલોડ કરો");
        gu.put("analyze_photo", "ફોટોનું વિશ્લેષણ કરો");
        gu.put("date", "તારીખ");
        gu.put("generated_via", "જય કિસાન એપ દ્વારા નિર્મિત • ભારતીય ખેતીનો સહારો");
        gu.put("home_detect_disease", "રોગની ઓળખ");
        gu.put("home_voice_assistant", "વોઇસ સહાયક");
        gu.put("home_mandi_prices", "બજાર ભાવ");
        gu.put("home_how_to_use", "કેવી રીતે ઉપયોગ કરવો");
        gu.put("home_weather_info", "હવામાન માહિતી");
        gu.put("home_farming_tools", "ખેતી સાધનો");
        gu.put("home_about", "પરિચય");
        gu.put("fertilizer_calculator", "ખાતર કેલ્ક્યુલેટર");
        gu.put("yield_estimator", "ઉપજ અંદાજ");
        gu.put("irrigation_planner", "પિયત આયોજક");
        gu.put("cost_calculator", "ખર્ચ કેલ્ક્યુલેટર");
        gu.put("voice_assistant", "વોઇસ સહાયક");
        gu.put("tap_to_ask", "પૂછવા માટે ટેપ કરો");
        gu.put("listening", "સાંભળી રહ્યું છે...");
        gu.put("thinking", "વિચારી રહ્યું છે...");
        gu.put("voice_greeting", "નમસ્તે! હું તમારી કૃષિ સહાય માટે અહીં છું.");
        gu.put("mandi_prices", "બજાર ભાવ");
        gu.put("select_crop", "પાક પસંદ કરો");
        gu.put("select_state", "રાજ્ય પસંદ કરો");
        gu.put("select_city", "જિલ્લો / માર્કેટ પસંદ કરો");
        gu.put("select_variety", "જાત પસંદ કરો");
        gu.put("select_date", "તારીખ પસંદ કરો");
        gu.put("get_prices", "બજાર ભાવ મેળવો");
        gu.put("market_insight", "બજાર વિશ્લેષણ");
        gu.put("price_range", "ભાવ મર્યાદા");
        gu.put("modal_price", "સરેરાશ ભાવ");
        gu.put("price_trend", "૭ દિવસનો ભાવ વલણ");
        gu.put("weather_impact", "હવામાન અસર");
        gu.put("farmer_advice", "ખેડૂત સલાહ");
        gu.put("sort_by", "ક્રમબદ્ધ કરો");
        gu.put("date_range", "તારીખ મર્યાદા");
        gu.put("production_costs", "૧. ઉત્પાદન ખર્ચ");
        gu.put("expected_harvest", "૨. અપેક્ષિત ઉપજ");
        gu.put("financial_analysis", "૩. નાણાકીય વિશ્લેષણ");
        gu.put("calculate_financials", "નફો-નુકસાન ગણો");
        gu.put("estimate_yield", "ઉપજનો અંદાજ લગાવો");
        gu.put("generate_plan", "સિંચાઈ યોજના બનાવો");
        gu.put("health_status", "આરોગ્ય સ્થિતિ");
        gu.put("survival_chance", "બચવાની શક્યતા");
        gu.put("summary", "સારાંશ");
        gu.put("recommended_actions", "શું કરવું (Do's)");
        gu.put("things_to_avoid", "શું ન કરવું (Don'ts)");
        gu.put("cures_fertilizers", "ઉપચાર અને ખાતર");
        gu.put("apply_to_tools", "સંબંધિત ખેતી સાધનો");
        gu.put("fertilizer_calc", "ખાતર ગણતરી");
        gu.put("cost_calc", "ખર્ચ ગણતરી");
        gu.put("irrigation_plan", "પિયત યોજના");
        gu.put("detect_location", "સ્થાન શોધો");
        gu.put("weather_title", "હવામાન માહિતી");
        gu.put("how_to_use", "માર્ગદર્શિકા");
        gu.put("about_developer", "ડેવલપર પરિચય");
        gu.put("general_variety", "સામાન્ય");
        gu.put("other_variety", "અન્ય");
        gu.put("alluvial_soil", "કાંપની જમીન");
        gu.put("black_soil", "કાળી જમીન");
        gu.put("red_soil", "રાતી જમીન");
        gu.put("laterite_soil", "પડખાઉ જમીન");
        gu.put("sandy_soil", "રેતાળ જમીન");
        gu.put("clay_soil", "ચીકણી જમીન");
        gu.put("flood_irrigation", "ધોરિયા પદ્ધતિ");
        gu.put("drip_irrigation", "ટપક સિંચાઈ");
        gu.put("sprinkler_irrigation", "ફુવારા પદ્ધતિ");
        gu.put("furrow_irrigation", "કાંસ પિયત");
        gu.put("basin_irrigation", "થાલા પિયત");
        gu.put("crop_wheat", "ઘઉં");
        gu.put("crop_rice", "ચોખા");
        gu.put("crop_maize", "મકાઈ");
        gu.put("crop_cotton", "કપાસ");
        gu.put("crop_sugarcane", "શેરડી");
        gu.put("crop_soybean", "સોયાબીન");
        gu.put("crop_mustard", "રાઈ");
        gu.put("crop_potato", "બટાટા");
        gu.put("crop_onion", "ડુંગળી");
        gu.put("crop_chili", "મરચું");
        gu.put("crop_tomato", "ટામેટા");
        gu.put("crop_paddy", "ડાંગર");
        gu.put("crop_millets", "બાજરી");
        gu.put("crop_pulses", "કઠોળ");
        gu.put("acre", "એકર");
        gu.put("hectare", "હેક્ટર");
        gu.put("bigha", "વીઘા");
        gu.put("kanal", "કનાલ");
        gu.put("biswa", "બિસ્વા");
        gu.put("cent", "સેન્ટ");
        gu.put("guntha", "ગુંઠા");
        gu.put("katha", "કઠ્ઠા");
        gu.put("decimal", "ડેસિમલ");
        gu.put("find_centers", "કૃષિ કેન્દ્રો શોધો");
        gu.put("healthy", "તંદુરસ્ત");
        gu.put("detected", "રોગ મળ્યો");
        gu.put("click_for_info", "વધુ માહિતી માટે ક્લિક કરો");
        gu.put("response_time", "પ્રતિસાદ સમય: %.1f સેકન્ડ");
        gu.put("note_optional_date", "નોંધ: તારીખ પસંદગી વૈકલ્પિક છે.");
        gu.put("order", "ક્રમ");
        gu.put("avg_yield", "સરેરાશ ઉપજ");
        gu.put("quintals", "ક્વિન્ટલ");
        gu.put("quintals_acre", "ક્વિન્ટલ/એકર");
        gu.put("irrigation_interval", "પિયતનો સમયગાળો");
        gu.put("days", "દિવસ");
        gu.put("unit", "એકમ");
        gu.put("area", "વિસ્તાર");
        gu.put("calculate_requirements", "જરૂરિયાત ગણો");
        DICTIONARY.put("gu", gu);

        Map<String, String> kn = new HashMap<>();
        kn.put("jai_kisan", "ಜೈ ಕಿಸಾನ್");
        kn.put("app_motto", "ನನ್ನ ಮಣ್ಣು, ನನ್ನ ರೈತ");
        kn.put("financial_pro_report", "ಹಣಕಾಸು ಪ್ರೊ ವರದಿ");
        kn.put("crop", "ಬೆಳೆ");
        kn.put("state", "ರಾಜ್ಯ");
        kn.put("city", "ಜಿಲ್ಲೆ / ಮಾರುಕಟ್ಟೆ");
        kn.put("variety", "ತಳಿ");
        kn.put("total_cost", "ಒಟ್ಟು ವೆಚ್ಚ");
        kn.put("cost_breakdown", "ವೆಚ್ಚದ ವಿವರ");
        kn.put("harvest_estimation", "ಇಳುವರಿ ಮತ್ತು ಆದಾಯ");
        kn.put("seed", "ಬೀಜ");
        kn.put("fertilizer", "ಗೊಬ್ಬರ");
        kn.put("labor", "ಕೂಲಿ");
        kn.put("other", "ಇತರೆ");
        kn.put("total_yield", "ಒಟ್ಟು ಇಳುವರಿ");
        kn.put("price", "ಬೆಲೆ");
        kn.put("total_revenue", "ಒಟ್ಟು ಆದಾಯ");
        kn.put("net_profit", "ನಿವ್ವಳ ಲಾಭ");
        kn.put("loss", "ನಷ್ಟ");
        kn.put("profit_margin", "ಲಾಭಾಂಶ ಶೇಕಡಾವಾರು");
        kn.put("break_even", "ವೆಚ್ಚ ಸಮತೋಲನ ಬೆಲೆ");
        kn.put("good_crop_choice", "ಉತ್ತಮ ಬೆಳೆ ಆಯ್ಕೆ");
        kn.put("medium_risk", "ಮಧ್ಯಮ ಅಪಾಯ");
        kn.put("warning_risk", "ಎಚ್ಚರಿಕೆ");
        kn.put("safe_harvest_tip", "ಸುರಕ್ಷಿತ ಇಳುವರಿ ಆದರೆ ವೆಚ್ಚಗಳನ್ನು ಗಮನಿಸಿ.");
        kn.put("low_margin_tip", "ಲಾಭಾಂಶ ತುಂಬಾ ಕಡಿಮೆ. ಅಪಾಯ ಹೆಚ್ಚಿದೆ.");
        kn.put("excellent_margin_tip", "ನಿಮ್ಮ ಹೂಡಿಕೆಯ ಆಧಾರದ ಮೇಲೆ ಅತ್ಯುತ್ತಮ ಲಾಭದ ನಿರೀಕ್ಷೆ ಇದೆ.");
        kn.put("land_area", "ಭೂಮಿ ವಿಸ್ತೀರ್ಣ");
        kn.put("recommended_fertilizer", "ಶಿಫಾರಸು ಮಾಡಿದ ರಸಗೊಬ್ಬರ");
        kn.put("shopping_list", "ಖರೀದಿ ಪಟ್ಟಿ");
        kn.put("liquid_option", "ದ್ರವ ಪರ್ಯಾಯ (Nano Urea)");
        kn.put("ai_advisory", "AI ತಜ್ಞರ ಸಲಹೆ");
        kn.put("urea", "ಯೂರಿಯಾ");
        kn.put("dap", "ಡಿಎಪಿ");
        kn.put("mop", "ಎಂಒಪಿ");
        kn.put("nitrogen", "ಸಾರಜನಕ");
        kn.put("phosphorus", "ರಂಜಕ");
        kn.put("potassium", "ಪೊಟ್ಯಾಶಿಯಂ");
        kn.put("soil_type", "ಮಣ್ಣಿನ ಪ್ರಕಾರ");
        kn.put("irrigation_method", "ನೀರಾವರಿ ವಿಧಾನ");
        kn.put("critical_stage", "ಪ್ರಮುಖ ಹಂತ");
        kn.put("expert_tip", "ತಜ್ಞರ ಸಲಹೆ");
        kn.put("irrigation_report", "ನೀರಾವರಿ ಯೋಜನೆ ವರದಿ");
        kn.put("yield_report", "ಇಳುವರಿ ಅಂದಾಜು ವರದಿ");
        kn.put("mandi_report", "ಮಾರುಕಟ್ಟೆ ದರ ವರದಿ");
        kn.put("diagnosis_report", "ಬೆಳೆ ಆರೋಗ್ಯ ವರದಿ");
        kn.put("voice_report", "ಧ್ವನಿ ಸಹಾಯಕ ವರದಿ");
        kn.put("share_report", "ವರದಿ ಹಂಚಿಕೊಳ್ಳಿ");
        kn.put("share_text", "ಪಠ್ಯವಾಗಿ ಹಂಚಿಕೊಳ್ಳಿ");
        kn.put("share_pdf", "ಪಿಡಿಎಫ್ ಹಂಚಿಕೊಳ್ಳಿ");
        kn.put("share_option", "ಹಂಚಿಕೆ ಆಯ್ಕೆ");
        kn.put("share_text_desc", "ವರದಿಯನ್ನು ಸಂದೇಶವಾಗಿ ಹಂಚಿಕೊಳ್ಳಿ.");
        kn.put("share_pdf_desc", "ಸುಂದರವಾದ ಪಿಡಿಎಫ್ ಡಾಕ್ಯುಮೆಂಟ್ ಹಂಚಿಕೊಳ್ಳಿ.");
        kn.put("cancel", "ರದ್ದುಮಾಡಿ");
        kn.put("settings", "ಸೆಟ್ಟಿಂಗ್‌ಗಳು");
        kn.put("language", "ಭಾಷೆ");
        kn.put("photo_expert", "ಫೋಟೋ ತಜ್ಞ");
        kn.put("detect_desc", "ಯಾವುದೇ ಬೆಳೆ, ಎಲೆ ಅಥವಾ ಹಣ್ಣನ್ನು ವಿಶ್ಲೇಷಿಸಿ. ತಜ್ಞರ ವರದಿ ಪಡೆಯಲು ಫೋಟೋ ತೆಗೆಯಿರಿ ಅಥವಾ ಅಪ್‌ಲೋಡ್ ಮಾಡಿ.");
        kn.put("no_image_selected", "ಯಾವುದೇ ಚಿತ್ರ ಆಯ್ಕೆಯಾಗಿಲ್ಲ");
        kn.put("capture", "ಫೋಟೋ ತೆಗೆಯಿರಿ");
        kn.put("upload", "ಅಪ್‌ಲೋಡ್ ಮಾಡಿ");
        kn.put("analyze_photo", "ಫೋಟೋ ವಿಶ್ಲೇಷಿಸಿ");
        kn.put("date", "ದಿನಾಂಕ");
        kn.put("generated_via", "ಜೈ ಕಿಸಾನ್ ಆ್ಯಪ್ ಮೂಲಕ ರಚಿಸಲಾಗಿದೆ • ಭಾರತೀಯ ಕೃಷಿಗೆ ಬೆಂಬಲ");
        kn.put("home_detect_disease", "ರೋಗ ಪತ್ತೆ");
        kn.put("home_voice_assistant", "ಧ್ವನಿ ಸಹಾಯಕ");
        kn.put("home_mandi_prices", "ಮಾರುಕಟ್ಟೆ ದರಗಳು");
        kn.put("home_how_to_use", "ಬಳಸುವುದು ಹೇಗೆ");
        kn.put("home_weather_info", "ಹವಾಮಾನ ಮಾಹಿತಿ");
        kn.put("home_farming_tools", "ಕೃಷಿ ಉಪಕರಣಗಳು");
        kn.put("home_about", "ಕುರಿತು");
        kn.put("fertilizer_calculator", "ಗೊಬ್ಬರ ಕ್ಯಾಲ್ಕುಲೇಟರ್");
        kn.put("yield_estimator", "ಇಳುವರಿ ಅಂದಾಜುಗಾರ");
        kn.put("irrigation_planner", "ನೀರಾವರಿ ಯೋಜಕ");
        kn.put("cost_calculator", "ವೆಚ್ಚ ಕ್ಯಾಲ್ಕುಲೇಟರ್");
        kn.put("voice_assistant", "ಧ್ವನಿ ಸಹಾಯಕ");
        kn.put("tap_to_ask", "ಕೇಳಲು ಟ್ಯಾಪ್ ಮಾಡಿ");
        kn.put("listening", "ಕೇಳಿಸಿಕೊಳ್ಳುತ್ತಿದ್ದೇನೆ...");
        kn.put("thinking", "ಯೋಚಿಸುತ್ತಿದ್ದೇನೆ...");
        kn.put("voice_greeting", "ನಮಸ್ಕಾರ! ನಿಮ್ಮ ಕೃಷಿ ಸಹಾಯಕ್ಕಾಗಿ ನಾನಿದ್ದೇನೆ.");
        kn.put("mandi_prices", "ಮಾರುಕಟ್ಟೆ ದರಗಳು");
        kn.put("select_crop", "ಬೆಳೆ ಆಯ್ಕೆಮಾಡಿ");
        kn.put("select_state", "ರಾಜ್ಯ ಆಯ್ಕೆಮಾಡಿ");
        kn.put("select_city", "ಜಿಲ್ಲೆ / ಮಾರುಕಟ್ಟೆ ಆಯ್ಕೆಮಾಡಿ");
        kn.put("select_variety", "ತಳಿ ಆಯ್ಕೆಮಾಡಿ");
        kn.put("select_date", "ದಿನಾಂಕ ಆಯ್ಕೆಮಾಡಿ");
        kn.put("get_prices", "ದರಗಳನ್ನು ಪಡೆಯಿರಿ");
        kn.put("market_insight", "ಮಾರುಕಟ್ಟೆ ಒಳನೋಟ");
        kn.put("price_range", "ಬೆಲೆ ಶ್ರೇಣಿ");
        kn.put("modal_price", "ಸರಾಸರಿ ದರ");
        kn.put("price_trend", "೭ ದಿನಗಳ ಬೆಲೆ ಪ್ರವೃತ್ತಿ");
        kn.put("weather_impact", "ಹವಾಮಾನ ಪರಿಣಾಮ");
        kn.put("farmer_advice", "ರೈತರಿಗೆ ಸಲಹೆ");
        kn.put("sort_by", "ವಿಂಗಡಿಸಿ");
        kn.put("date_range", "ದಿನಾಂಕ ಶ್ರೇಣಿ");
        kn.put("production_costs", "೧. ಉತ್ಪಾದನಾ ವೆಚ್ಚಗಳು");
        kn.put("expected_harvest", "೨. ನಿರೀಕ್ಷಿತ ಇಳುವರಿ");
        kn.put("financial_analysis", "೩. ಆರ್ಥಿಕ ವಿಶ್ಲೇಷಣೆ");
        kn.put("calculate_financials", "ಆದಾಯ-ವೆಚ್ಚ ಲೆಕ್ಕಹಾಕಿ");
        kn.put("estimate_yield", "ಇಳುವರಿ ಅಂದಾಜಿಸಿ");
        kn.put("generate_plan", "ನೀರಾವರಿ ಯೋಜನೆ ರಚಿಸಿ");
        kn.put("health_status", "ಆರೋಗ್ಯ ಸ್ಥಿತಿ");
        kn.put("survival_chance", "ಉಳಿಯುವ ಸಾಧ್ಯತೆ");
        kn.put("summary", "ಸಾರಾಂಶ");
        kn.put("recommended_actions", "ಮಾಡಬೇಕಾದದ್ದು (Do's)");
        kn.put("things_to_avoid", "ಮಾಡಬಾರದ್ದು (Don'ts)");
        kn.put("cures_fertilizers", "ಪರಿಹಾರ ಮತ್ತು ರಸಗೊಬ್ಬರಗಳು");
        kn.put("apply_to_tools", "ಸಂಬಂಧಿತ ಕೃಷಿ ಪರಿಕರಗಳು");
        kn.put("fertilizer_calc", "ಗೊಬ್ಬರ ಲೆಕ್ಕಾಚಾರ");
        kn.put("cost_calc", "ವೆಚ್ಚ ಲೆಕ್ಕಾಚಾರ");
        kn.put("irrigation_plan", "ನೀರಾವರಿ ಯೋಜನೆ");
        kn.put("detect_location", "ಸ್ಥಳ ಪತ್ತೆ ಮಾಡಿ");
        kn.put("weather_title", "ಹವಾಮಾನ ಮುನ್ಸೂಚನೆ");
        kn.put("how_to_use", "ಬಳಕೆ ಕೈಪಿಡಿ");
        kn.put("about_developer", "ಡೆವಲಪರ್ ಮಾಹಿತಿ");
        kn.put("general_variety", "ಸಾಮಾನ್ಯ");
        kn.put("other_variety", "ಇತರೆ");
        kn.put("alluvial_soil", "ಮೆಕ್ಕಲು ಮಣ್ಣು");
        kn.put("black_soil", "ಕಪ್ಪು ಮಣ್ಣು");
        kn.put("red_soil", "ಕೆಂಪು ಮಣ್ಣು");
        kn.put("laterite_soil", "ಲ್ಯಾಟರೈಟ್ ಮಣ್ಣು");
        kn.put("sandy_soil", "ಮರಳು ಮಣ್ಣು");
        kn.put("clay_soil", "ಜೇಡಿ ಮಣ್ಣು");
        kn.put("flood_irrigation", "ಹರಿ ನೀರಾವರಿ");
        kn.put("drip_irrigation", "ಹನಿ ನೀರಾವರಿ");
        kn.put("sprinkler_irrigation", "ತುಂತುರು ನೀರಾವರಿ");
        kn.put("furrow_irrigation", "ಸಾಲು ನೀರಾವರಿ");
        kn.put("basin_irrigation", "ಪಾತಿ ನೀರಾವರಿ");
        kn.put("crop_wheat", "ಗೋಧಿ");
        kn.put("crop_rice", "ಅಕ್ಕಿ");
        kn.put("crop_maize", "ಮೆಕ್ಕೆಜೋಳ");
        kn.put("crop_cotton", "ಹತ್ತಿ");
        kn.put("crop_sugarcane", "ಕಬ್ಬು");
        kn.put("crop_soybean", "ಸೋಯಾಬೀನ್");
        kn.put("crop_mustard", "ಸಾಸಿವೆ");
        kn.put("crop_potato", "ಆಲೂಗಡ್ಡೆ");
        kn.put("crop_onion", "ಈರುಳ್ಳಿ");
        kn.put("crop_chili", "ಮೆಣಸಿನಕಾಯಿ");
        kn.put("crop_tomato", "ಟೊಮೆಟೊ");
        kn.put("crop_paddy", "ಭತ್ತ");
        kn.put("crop_millets", "ಸಜ್ಜೆ");
        kn.put("crop_pulses", "ಬೇಳೆಕಾಳುಗಳು");
        kn.put("acre", "ಎಕರೆ");
        kn.put("hectare", "ಹೆಕ್ಟೇರ್");
        kn.put("bigha", "ಬಿಘಾ");
        kn.put("kanal", "ಕೆನಾಲ್");
        kn.put("biswa", "ಬಿಸ್ವಾ");
        kn.put("cent", "ಸೆಂಟ್");
        kn.put("guntha", "ಗುಂಟಾ");
        kn.put("katha", "ಕಥಾ");
        kn.put("decimal", "ಡೆಸಿಮಲ್");
        kn.put("find_centers", "ಕೃಷಿ ಕೇಂದ್ರಗಳನ್ನು ಹುಡುಕಿ");
        kn.put("healthy", "ಆರೋಗ್ಯಕರ");
        kn.put("detected", "ಪತ್ತೆಯಾದ ರೋಗ");
        kn.put("click_for_info", "ಹೆಚ್ಚಿನ ಮಾಹಿತಿಗಾಗಿ ಕ್ಲಿಕ್ ಮಾಡಿ");
        kn.put("response_time", "ಪ್ರತಿಕ್ರಿಯೆ ಸಮಯ: %.1f ಸೆಕೆಂಡುಗಳು");
        kn.put("note_optional_date", "ಸೂಚನೆ: ದಿನಾಂಕ ಆಯ್ಕೆ ಐಚ್ಛಿಕ.");
        kn.put("order", "ಕ್ರಮ");
        kn.put("avg_yield", "ಸರಾಸರಿ ಇಳುವರಿ");
        kn.put("quintals", "ಕ್ವಿಂಟಾಲ್");
        kn.put("quintals_acre", "ಕ್ವಿಂಟಾಲ್/ಎಕರೆ");
        kn.put("irrigation_interval", "ನೀರಾವರಿ ಅಂತರ");
        kn.put("days", "ದಿನಗಳು");
        kn.put("unit", "ಘಟಕ");
        kn.put("area", "ವಿಸ್ತೀರ್ಣ");
        kn.put("calculate_requirements", "ಅಗತ್ಯವನ್ನು ಲೆಕ್ಕಹಾಕಿ");
        DICTIONARY.put("kn", kn);

        Map<String, String> ml = new HashMap<>();
        ml.put("jai_kisan", "ജയ് കിസാൻ");
        ml.put("app_motto", "എന്റെ മണ്ണ്, എന്റെ കർഷകൻ");
        ml.put("financial_pro_report", "സാമ്പത്തിക പ്രോ റിപ്പോർട്ട്");
        ml.put("crop", "വിള");
        ml.put("state", "സംസ്ഥാനം");
        ml.put("city", "ജില്ല / മാർക്കറ്റ്");
        ml.put("variety", "ഇനം");
        ml.put("total_cost", "ആകെ ചെലവ്");
        ml.put("cost_breakdown", "ചെലവ് വിവരങ്ങൾ");
        ml.put("harvest_estimation", "വിളവും വരുമാനവും");
        ml.put("seed", "വിത്ത്");
        ml.put("fertilizer", "വളം");
        ml.put("labor", "തൊഴിലാളി");
        ml.put("other", "മറ്റുള്ളവ");
        ml.put("total_yield", "ആകെ വിളവ്");
        ml.put("price", "വില");
        ml.put("total_revenue", "ആകെ വരുമാനം");
        ml.put("net_profit", "അറ്റാദായം");
        ml.put("loss", "നഷ്ടം");
        ml.put("profit_margin", "ലാഭ നിരക്ക്");
        ml.put("break_even", "മുതൽമുടക്ക് തിരിച്ചുകിട്ടുന്ന വില");
        ml.put("good_crop_choice", "നല്ല വിള തിരഞ്ഞെടുപ്പ്");
        ml.put("medium_risk", "ഇടത്തരം അപകടസാധ്യത");
        ml.put("warning_risk", "മുന്നറിയിപ്പ്");
        ml.put("safe_harvest_tip", "നല്ല വിളവ് പ്രതീക്ഷിക്കാം, ചെലവുകൾ ശ്രദ്ധിക്കുക.");
        ml.put("low_margin_tip", "ലാഭ നിരക്ക് വളരെ കുറവാണ്. നഷ്ട സാധ്യതയുണ്ട്.");
        ml.put("excellent_margin_tip", "നിങ്ങളുടെ വിവരങ്ങൾ പ്രകാരം മികച്ച ലാഭ സാധ്യതയുണ്ട്.");
        ml.put("land_area", "ഭൂമിയുടെ വിസ്തീർണ്ണം");
        ml.put("recommended_fertilizer", "ശുപാർശ ചെയ്യുന്ന വളം");
        ml.put("shopping_list", "വാങ്ങൽ പട്ടിക");
        ml.put("liquid_option", "ദ്രാവക വളം (Nano Urea)");
        ml.put("ai_advisory", "AI വിദഗ്ദ്ധ ഉപദേശം");
        ml.put("urea", "യൂറിയ");
        ml.put("dap", "ഡിഎപി");
        ml.put("mop", "എംഒപി");
        ml.put("nitrogen", "നൈട്രജൻ");
        ml.put("phosphorus", "ഫോസ്ഫറസ്");
        ml.put("potassium", "പൊട്ടാസ്യം");
        ml.put("soil_type", "മണ്ണിന്റെ തരം");
        ml.put("irrigation_method", "നനയ്ക്കൽ രീതി");
        ml.put("critical_stage", "നിർണായക ഘട്ടം");
        ml.put("expert_tip", "വിദഗ്ദ്ധ നിർദ്ദേശം");
        ml.put("irrigation_report", "ജലസേചന പദ്ധതി റിപ്പോർട്ട്");
        ml.put("yield_report", "വിളവ് കണക്കാക്കൽ റിപ്പോർട്ട്");
        ml.put("mandi_report", "മാർക്കറ്റ് വില റിപ്പോർട്ട്");
        ml.put("diagnosis_report", "വിള ആരോഗ്യ റിപ്പോർട്ട്");
        ml.put("voice_report", "വോയ്സ് അസിസ്റ്റന്റ് റിപ്പോർട്ട്");
        ml.put("share_report", "റിപ്പോർട്ട് പങ്കിടുക");
        ml.put("share_text", "ടെക്സ്റ്റായി പങ്കിടുക");
        ml.put("share_pdf", "പിഡിഎഫ് പങ്കിടുക");
        ml.put("share_option", "പങ്കിടൽ ഓപ്ഷൻ");
        ml.put("share_text_desc", "റിപ്പോർട്ട് സന്ദേശമായി പങ്കിടുക.");
        ml.put("share_pdf_desc", "മനോഹരമായ ഒരു പിഡിഎഫ് പ്രമാണം പങ്കിടുക.");
        ml.put("cancel", "റദ്ദാക്കുക");
        ml.put("settings", "ക്രമീകരണങ്ങൾ");
        ml.put("language", "ഭാഷ");
        ml.put("photo_expert", "ഫോട്ടോ വിദഗ്ദ്ധൻ");
        ml.put("detect_desc", "ഏതെങ്കിലും വിള, ഇല അല്ലെങ്കിൽ പഴം വിശകലനം ചെയ്യുക. വിദഗ്ദ്ധ റിപ്പോർട്ടിനായി ഫോട്ടോ എടുക്കുക അല്ലെങ്കിൽ അപ്‌ലോഡ് ചെയ്യുക.");
        ml.put("no_image_selected", "ചിത്രം തിരഞ്ഞെടുത്തിട്ടില്ല");
        ml.put("capture", "ഫോട്ടോ എടുക്കുക");
        ml.put("upload", "അപ്‌ലോഡ് ചെയ്യുക");
        ml.put("analyze_photo", "ഫോട്ടോ വിശകലനം ചെയ്യുക");
        ml.put("date", "തീയതി");
        ml.put("generated_via", "ജയ് കിസാൻ ആപ്പ് വഴി തയ്യാറാക്കിയത് • ഇന്ത്യൻ കാർഷിക രംഗത്തിന് കരുത്ത്");
        ml.put("home_detect_disease", "രോഗനിർണയം");
        ml.put("home_voice_assistant", "വോയ്സ് അസിസ്റ്റന്റ്");
        ml.put("home_mandi_prices", "മാർക്കറ്റ് നിരക്കുകൾ");
        ml.put("home_how_to_use", "ഉപയോഗിക്കേണ്ട വിധം");
        ml.put("home_weather_info", "കാലാവസ്ഥ വിവരം");
        ml.put("home_farming_tools", "കാർഷിക ഉപകരണങ്ങൾ");
        ml.put("home_about", "വിവരണം");
        ml.put("fertilizer_calculator", "വള കാൽക്കുലേറ്റർ");
        ml.put("yield_estimator", "വിളവ് എസ്റ്റിമേറ്റർ");
        ml.put("irrigation_planner", "ജലസേചന പ്ലാനർ");
        ml.put("cost_calculator", "ചെലവ് കാൽക്കുലേറ്റർ");
        ml.put("voice_assistant", "വോയ്സ് അസിസ്റ്റന്റ്");
        ml.put("tap_to_ask", "ചോദിക്കാൻ ടാപ്പ് ചെയ്യുക");
        ml.put("listening", "കേൾക്കുന്നു...");
        ml.put("thinking", "ചിന്തിക്കുന്നു...");
        ml.put("voice_greeting", "നമസ്കാരം! നിങ്ങളുടെ കാർഷിക സഹായത്തിനായി ഞാൻ ഇവിടെയുണ്ട്.");
        ml.put("mandi_prices", "മാർക്കറ്റ് നിരക്കുകൾ");
        ml.put("select_crop", "വിള തിരഞ്ഞെടുക്കുക");
        ml.put("select_state", "സംസ്ഥാനം തിരഞ്ഞെടുക്കുക");
        ml.put("select_city", "ജില്ല / മാർക്കറ്റ് തിരഞ്ഞെടുക്കുക");
        ml.put("select_variety", "ഇനം തിരഞ്ഞെടുക്കുക");
        ml.put("select_date", "തീയതി തിരഞ്ഞെടുക്കുക");
        ml.put("get_prices", "വില വിവരങ്ങൾ നേടുക");
        ml.put("market_insight", "വിപണി വിശകലനം");
        ml.put("price_range", "വില പരിധി");
        ml.put("modal_price", "ശരാശരി നിരക്ക്");
        ml.put("price_trend", "7-ദിവസത്തെ വില പ്രവണത");
        ml.put("weather_impact", "കാലാവസ്ഥ പ്രഭാവം");
        ml.put("farmer_advice", "കർഷക ഉപദേശം");
        ml.put("sort_by", "ക്രമപ്പെടുത്തുക");
        ml.put("date_range", "തീയതി പരിധി");
        ml.put("production_costs", "1. ഉൽപ്പാദന ചെലവുകൾ");
        ml.put("expected_harvest", "2. പ്രതീക്ഷിക്കുന്ന വിളവ്");
        ml.put("financial_analysis", "3. സാമ്പത്തിക വിശകലനം");
        ml.put("calculate_financials", "ലാഭ-നഷ്ടം കണക്കാക്കുക");
        ml.put("estimate_yield", "വിളവ് കണക്കാക്കുക");
        ml.put("generate_plan", "ജലസേചന പദ്ധതി തയ്യാറാക്കുക");
        ml.put("health_status", "ആരോഗ്യ നില");
        ml.put("survival_chance", "അതിജീവന സാധ്യത");
        ml.put("summary", "സംഗ്രഹം");
        ml.put("recommended_actions", "ചെയ്യേണ്ട കാര്യങ്ങൾ (Do's)");
        ml.put("things_to_avoid", "ഒഴിവാക്കേണ്ട കാര്യങ്ങൾ (Don'ts)");
        ml.put("cures_fertilizers", "ചികിത്സകളും വളങ്ങളും");
        ml.put("apply_to_tools", "ബന്ധപ്പെട്ട കൃഷി ഉപകരണങ്ങൾ");
        ml.put("fertilizer_calc", "വളത്തിന്റെ അളവ്");
        ml.put("cost_calc", "ചെലവ് കണക്ക്");
        ml.put("irrigation_plan", "ജലസേചന പദ്ധതി");
        ml.put("detect_location", "സ്ഥലം കണ്ടെത്തുക");
        ml.put("weather_title", "കാലാവസ്ഥ വിവരം");
        ml.put("how_to_use", "ഉപയോഗ സഹായി");
        ml.put("about_developer", "വികസന വിവരങ്ങൾ");
        ml.put("general_variety", "സാധാരണ");
        ml.put("other_variety", "മറ്റുള്ളവ");
        ml.put("alluvial_soil", "എക്കൽ മണ്ണ്");
        ml.put("black_soil", "കറുത്ത മണ്ണ്");
        ml.put("red_soil", "ചെമ്മണ്ണ്");
        ml.put("laterite_soil", "ലാറ്ററൈറ്റ് മണ്ണ്");
        ml.put("sandy_soil", "മണൽ മണ്ണ്");
        ml.put("clay_soil", "കളിമണ്ണ്");
        ml.put("flood_irrigation", "വെള്ളം കയറ്റൽ");
        ml.put("drip_irrigation", "തുള്ളി നന");
        ml.put("sprinkler_irrigation", "തളിക്കൽ നന");
        ml.put("furrow_irrigation", "ചാൽ നന");
        ml.put("basin_irrigation", "തടം നന");
        ml.put("crop_wheat", "ഗോതമ്പ്");
        ml.put("crop_rice", "അരി");
        ml.put("crop_maize", "ചോളം");
        ml.put("crop_cotton", "പരുത്തി");
        ml.put("crop_sugarcane", "കരിമ്പ്");
        ml.put("crop_soybean", "സോയാബീൻ");
        ml.put("crop_mustard", "കടുക്");
        ml.put("crop_potato", "ഉരുളക്കിഴങ്ങ്");
        ml.put("crop_onion", "ഉള്ളി");
        ml.put("crop_chili", "മുളക്");
        ml.put("crop_tomato", "തക്കാളി");
        ml.put("crop_paddy", "നെല്ല്");
        ml.put("crop_millets", "തിന");
        ml.put("crop_pulses", "പയറുവർഗ്ഗങ്ങൾ");
        ml.put("acre", "ഏക്കർ");
        ml.put("hectare", "ഹെക്ടർ");
        ml.put("bigha", "ബിഘ");
        ml.put("kanal", "കനാൽ");
        ml.put("biswa", "ബിസ്വ");
        ml.put("cent", "സെന്റ്");
        ml.put("guntha", "ഗുണ്ട");
        ml.put("katha", "കഥ");
        ml.put("decimal", "ഡെസിമൽ");
        ml.put("find_centers", "കൃഷി കേന്ദ്രങ്ങൾ കണ്ടെത്തുക");
        ml.put("healthy", "ആരോഗ്യമുള്ളത്");
        ml.put("detected", "കണ്ടെത്തിയ രോഗം");
        ml.put("click_for_info", "കൂടുതൽ വിവരങ്ങൾക്ക് ക്ലിക്ക് ചെയ്യുക");
        ml.put("response_time", "പ്രതികരണ സമയം: %.1f സെക്കൻഡ്");
        ml.put("note_optional_date", "ശ്രദ്ധിക്കുക: തീയതി തിരഞ്ഞെടുക്കൽ നിർബന്ധമില്ല.");
        ml.put("order", "ക്രമം");
        ml.put("avg_yield", "ശരാശരി വിളവ്");
        ml.put("quintals", "ക്വിന്റൽ");
        ml.put("quintals_acre", "ക്വിന്റൽ/ഏക്കർ");
        ml.put("irrigation_interval", "നനയ്ക്കൽ ഇടവേള");
        ml.put("days", "ദിവസങ്ങൾ");
        ml.put("unit", "യൂണിറ്റ്");
        ml.put("area", "വിസ്തീർണ്ണം");
        ml.put("calculate_requirements", "ആവശ്യം കണക്കാക്കുക");
        DICTIONARY.put("ml", ml);

        Map<String, String> pa = new HashMap<>();
        pa.put("jai_kisan", "ਜੈ ਕਿਸਾਨ");
        pa.put("app_motto", "ਮੇਰੀ ਮਿੱਟੀ, ਮੇਰੇ ਕਿਸਾਨ");
        pa.put("financial_pro_report", "ਵਿੱਤੀ ਪ੍ਰੋ ਰਿਪੋਰਟ");
        pa.put("crop", "ਫਸਲ");
        pa.put("state", "ਰਾਜ");
        pa.put("city", "ਜ਼ਿਲ੍ਹਾ / ਮੰਡੀ");
        pa.put("variety", "ਕਿਸਮ");
        pa.put("total_cost", "ਕੁੱਲ ਖਰਚਾ");
        pa.put("cost_breakdown", "ਖਰਚੇ ਦਾ ਵੇਰਵਾ");
        pa.put("harvest_estimation", "ਝਾੜ ਅਤੇ ਆਮਦਨ");
        pa.put("seed", "ਬੀਜ");
        pa.put("fertilizer", "ਖਾਦ");
        pa.put("labor", "ਮਜ਼ਦੂਰ");
        pa.put("other", "ਹੋਰ");
        pa.put("total_yield", "ਕੁੱਲ ਝਾੜ");
        pa.put("price", "ਭਾਅ");
        pa.put("total_revenue", "ਕੁੱਲ ਆਮਦਨ");
        pa.put("net_profit", "ਸ਼ੁੱਧ ਮੁਨਾਫ਼ਾ");
        pa.put("loss", "ਨੁਕਸਾਨ");
        pa.put("profit_margin", "ਮੁਨਾਫ਼ਾ ਫ਼ੀਸਦੀ");
        pa.put("break_even", "ਲਾਗਤ-ਮੁਕਤ ਭਾਅ");
        pa.put("good_crop_choice", "ਵਧੀਆ ਫਸਲ ਚੋਣ");
        pa.put("medium_risk", "ਦਰਮਿਆਨਾ ਜੋਖਮ");
        pa.put("warning_risk", "ਚੇਤਾਵਨੀ");
        pa.put("safe_harvest_tip", "ਸੁਰੱਖਿਅਤ ਝਾੜ ਹੈ ਪਰ ਖਰਚਿਆਂ ਦਾ ਧਿਆਨ ਰੱਖੋ।");
        pa.put("low_margin_tip", "ਮੁਨਾਫ਼ਾ ਬਹੁਤ ਘੱਟ ਹੈ। ਜੋਖਮ ਵੱਧ ਹੈ।");
        pa.put("excellent_margin_tip", "ਤੁਹਾਡੇ ਨਿਵੇਸ਼ ਅਨੁਸਾਰ ਸ਼ਾਨਦਾਰ ਮੁਨਾਫ਼ੇ ਦੀ ਸੰਭਾਵਨਾ ਹੈ।");
        pa.put("land_area", "ਜ਼ਮੀਨ ਦਾ ਰਕਬਾ");
        pa.put("recommended_fertilizer", "ਸਿਫ਼ਾਰਸ਼ ਕੀਤੀ ਖਾਦ");
        pa.put("shopping_list", "ਖਰੀਦ ਸੂਚੀ");
        pa.put("liquid_option", "ਤਰਲ ਵਿਕਲਪ (Nano Urea)");
        pa.put("ai_advisory", "AI ਮਾਹਿਰ ਸਲਾਹ");
        pa.put("urea", "ਯੂਰੀਆ");
        pa.put("dap", "ਡੀਏਪੀ");
        pa.put("mop", "ਐਮਓਪੀ");
        pa.put("nitrogen", "ਨਾਈਟ੍ਰੋਜਨ");
        pa.put("phosphorus", "ਫਾਸਫੋਰਸ");
        pa.put("potassium", "ਪੋਟਾਸ਼ੀਅਮ");
        pa.put("soil_type", "ਮਿੱਟੀ ਦੀ ਕਿਸਮ");
        pa.put("irrigation_method", "ਸਿੰਚਾਈ ਢੰਗ");
        pa.put("critical_stage", "ਨਾਜ਼ੁਕ ਪੜਾਅ");
        pa.put("expert_tip", "ਮਾਹਿਰ ਸੁਝਾਅ");
        pa.put("irrigation_report", "ਸਿੰਚਾਈ ਯੋਜਨਾ ਰਿਪੋਰਟ");
        pa.put("yield_report", "ਝਾੜ ਅਨੁਮਾਨ ਰਿਪੋਰਟ");
        pa.put("mandi_report", "ਮੰਡੀ ਭਾਅ ਰਿਪੋਰਟ");
        pa.put("diagnosis_report", "ਫਸਲ ਸਿਹਤ ਰਿਪੋਰਟ");
        pa.put("voice_report", "ਆਵਾਜ਼ ਸਹਾਇਕ ਰਿਪੋਰਟ");
        pa.put("share_report", "ਰਿਪੋਰਟ ਸਾਂਝੀ ਕਰੋ");
        pa.put("share_text", "ਟੈਕਸਟ ਵਜੋਂ ਸਾਂਝਾ ਕਰੋ");
        pa.put("share_pdf", "ਪੀਡੀਐਫ ਸਾਂਝਾ ਕਰੋ");
        pa.put("share_option", "ਸਾਂਝਾ ਕਰਨ ਦਾ ਵਿਕਲਪ");
        pa.put("share_text_desc", "ਰਿਪੋਰਟ ਨੂੰ ਸੁਨੇਹੇ ਵਜੋਂ ਸਾਂਝਾ ਕਰੋ।");
        pa.put("share_pdf_desc", "ਇੱਕ ਸੁੰਦਰ ਪੀਡੀਐਫ ਦਸਤਾਵੇਜ਼ ਸਾਂਝਾ ਕਰੋ।");
        pa.put("cancel", "ਰੱਦ ਕਰੋ");
        pa.put("settings", "ਸੈਟਿੰਗਾਂ");
        pa.put("language", "ਭਾਸ਼ਾ");
        pa.put("photo_expert", "ਫੋਟੋ ਮਾਹਿਰ");
        pa.put("detect_desc", "ਕਿਸੇ ਵੀ ਫਸਲ, ਪੱਤੇ ਜਾਂ ਫਲ ਦਾ ਵਿਸ਼ਲੇਸ਼ਣ ਕਰੋ। ਮਾਹਿਰ ਰਿਪੋਰਟ ਪ੍ਰਾਪਤ ਕਰਨ ਲਈ ਫੋਟੋ ਖਿੱਚੋ ਜਾਂ ਅਪਲੋਡ ਕਰੋ।");
        pa.put("no_image_selected", "ਕੋਈ ਫੋਟੋ ਨਹੀਂ ਚੁਣੀ ਗਈ");
        pa.put("capture", "ਫੋਟੋ ਲਓ");
        pa.put("upload", "ਅਪਲੋਡ ਕਰੋ");
        pa.put("analyze_photo", "ਫੋਟੋ ਦਾ ਵਿਸ਼ਲੇਸ਼ਣ ਕਰੋ");
        pa.put("date", "ਮਿਤੀ");
        pa.put("generated_via", "ਜੈ ਕਿਸਾਨ ਐਪ ਰਾਹੀਂ ਤਿਆਰ • ਭਾਰਤੀ ਖੇਤੀ ਦਾ ਸਹਾਰਾ");
        pa.put("home_detect_disease", "ਬਿਮਾਰੀ ਦੀ ਪਛਾਣ");
        pa.put("home_voice_assistant", "ਆਵਾਜ਼ ਸਹਾਇਕ");
        pa.put("home_mandi_prices", "ਮੰਡੀ ਦੇ ਭਾਅ");
        pa.put("home_how_to_use", "ਕਿਵੇਂ ਵਰਤਣਾ ਹੈ");
        pa.put("home_weather_info", "ਮੌਸਮ ਜਾਣਕਾਰੀ");
        pa.put("home_farming_tools", "ਖੇਤੀ ਸੰਦ");
        pa.put("home_about", "ਬਾਰੇ");
        pa.put("fertilizer_calculator", "ਖਾਦ ਕੈਲਕੁਲੇਟਰ");
        pa.put("yield_estimator", "ਝਾੜ ਦਾ ਅੰਦਾਜ਼ਾ");
        pa.put("irrigation_planner", "ਸਿੰਚਾਈ ਯੋਜਨਾ");
        pa.put("cost_calculator", "ਲਾਗਤ ਕੈਲਕੁਲੇਟਰ");
        pa.put("voice_assistant", "ਆਵਾਜ਼ ਸਹਾਇਕ");
        pa.put("tap_to_ask", "ਪੁੱਛਣ ਲਈ ਟੈਪ ਕਰੋ");
        pa.put("listening", "ਸੁਣ ਰਿਹਾ ਹਾਂ...");
        pa.put("thinking", "ਸੋਚ ਰਿਹਾ ਹਾਂ...");
        pa.put("voice_greeting", "ਸਤਿ ਸ੍ਰੀ ਅਕਾਲ! ਮੈਂ ਤੁਹਾਡੀ ਖੇਤੀ ਸਹਾਇਤਾ ਲਈ ਇੱਥੇ ਹਾਂ।");
        pa.put("mandi_prices", "ਮੰਡੀ ਦੇ ਭਾਅ");
        pa.put("select_crop", "ਫਸਲ ਚੁਣੋ");
        pa.put("select_state", "ਰਾਜ ਚੁਣੋ");
        pa.put("select_city", "ਜ਼ਿਲ੍ਹਾ / ਮੰਡੀ ਚੁਣੋ");
        pa.put("select_variety", "ਕਿਸਮ ਚੁਣੋ");
        pa.put("select_date", "ਤਾਰੀਖ ਚੁਣੋ");
        pa.put("get_prices", "ਭਾਅ ਪ੍ਰਾਪਤ ਕਰੋ");
        pa.put("market_insight", "ਮੰਡੀ ਜਾਣਕਾਰੀ");
        pa.put("price_range", "ਭਾਅ ਦਾ ਦਾਇਰਾ");
        pa.put("modal_price", "ਔਸਤ ਭਾਅ");
        pa.put("price_trend", "੭ ਦਿਨਾਂ ਦਾ ਭਾਅ ਰੁਝਾਨ");
        pa.put("weather_impact", "ਮੌਸਮ ਦਾ ਅਸਰ");
        pa.put("farmer_advice", "ਕਿਸਾਨ ਸਲਾਹ");
        pa.put("sort_by", "ਤਰਤੀਬ ਦਿਓ");
        pa.put("date_range", "ਤਾਰੀਖ ਸੀਮਾ");
        pa.put("production_costs", "੧. ਉਤਪਾਦਨ ਲਾਗਤ");
        pa.put("expected_harvest", "੨. ਅਨੁਮਾਨਿਤ ਝਾੜ");
        pa.put("financial_analysis", "੩. ਵਿੱਤੀ ਵਿਸ਼ਲੇਸ਼ਣ");
        pa.put("calculate_financials", "ਲਾਭ-ਹਾਨੀ ਦੀ ਗਣਨਾ ਕਰੋ");
        pa.put("estimate_yield", "ਝਾੜ ਦਾ ਅੰਦਾਜ਼ਾ ਲਗਾਓ");
        pa.put("generate_plan", "ਸਿੰਚਾਈ ਯੋਜਨਾ ਬਣਾਓ");
        pa.put("health_status", "ਸਿਹਤ ਸਥਿਤੀ");
        pa.put("survival_chance", "ਬਚਣ ਦੀ ਸੰਭਾਵਨਾ");
        pa.put("summary", "ਸੰਖੇਪ");
        pa.put("recommended_actions", "ਕੀ ਕਰਨਾ ਹੈ (Do's)");
        pa.put("things_to_avoid", "ਕੀ ਨਹੀਂ ਕਰਨਾ (Don'ts)");
        pa.put("cures_fertilizers", "ਇਲਾਜ ਅਤੇ ਖਾਦਾਂ");
        pa.put("apply_to_tools", "ਸੰਬੰਧਿਤ ਖੇਤੀ ਸੰਦ");
        pa.put("fertilizer_calc", "ਖਾਦ ਕੈਲਕੁਲੇਟਰ");
        pa.put("cost_calc", "ਖਰਚਾ ਕੈਲਕੁਲੇਟਰ");
        pa.put("irrigation_plan", "ਸਿੰਚਾਈ ਯੋਜਨਾ");
        pa.put("detect_location", "ਸਥਿਤੀ ਪਤਾ ਕਰੋ");
        pa.put("weather_title", "ਮੌਸਮ ਜਾਣਕਾਰੀ");
        pa.put("how_to_use", "ਵਰਤੋਂ ਨਿਰਦੇਸ਼");
        pa.put("about_developer", "ਡਿਵੈਲਪਰ ਬਾਰੇ");
        pa.put("general_variety", "ਆਮ");
        pa.put("other_variety", "ਹੋਰ");
        pa.put("alluvial_soil", "ਜਲੋਢ ਮਿੱਟੀ");
        pa.put("black_soil", "ਕਾਲੀ ਮਿੱਟੀ");
        pa.put("red_soil", "ਲਾਲ ਮਿੱਟੀ");
        pa.put("laterite_soil", "ਲੈਟਰਾਈਟ ਮਿੱਟੀ");
        pa.put("sandy_soil", "ਰੇਤਲੀ ਮਿੱਟੀ");
        pa.put("clay_soil", "ਚੀਕਣੀ ਮਿੱਟੀ");
        pa.put("flood_irrigation", "ਸਤਹੀ ਸਿੰਚਾਈ");
        pa.put("drip_irrigation", "ਡ੍ਰਿਪ ਸਿੰਚਾਈ (ਤੁਪਕਾ)");
        pa.put("sprinkler_irrigation", "ਫੁਹਾਰਾ ਸਿੰਚਾਈ");
        pa.put("furrow_irrigation", "ਖਾਲੀ ਸਿੰਚਾਈ");
        pa.put("basin_irrigation", "ਥਾਲਾ ਸਿੰਚਾਈ");
        pa.put("crop_wheat", "ਕਣਕ");
        pa.put("crop_rice", "ਚੌਲ");
        pa.put("crop_maize", "ਮੱਕੀ");
        pa.put("crop_cotton", "ਕਪਾਹ");
        pa.put("crop_sugarcane", "ਗੰਨਾ");
        pa.put("crop_soybean", "ਸੋਇਆਬੀਨ");
        pa.put("crop_mustard", "ਸਰ੍ਹੋਂ");
        pa.put("crop_potato", "ਆਲੂ");
        pa.put("crop_onion", "ਪਿਆਜ਼");
        pa.put("crop_chili", "ਮਿਰਚ");
        pa.put("crop_tomato", "ਟਮਾਟਰ");
        pa.put("crop_paddy", "ਝੋਨਾ");
        pa.put("crop_millets", "ਬਾਜਰਾ");
        pa.put("crop_pulses", "ਦਾਲਾਂ");
        pa.put("acre", "ਏਕੜ");
        pa.put("hectare", "ਹੈਕਟੇਅਰ");
        pa.put("bigha", "ਬੀਘਾ");
        pa.put("kanal", "ਕਨਾਲ");
        pa.put("biswa", "ਬਿਸਵਾ");
        pa.put("cent", "ਸੈਂਟ");
        pa.put("guntha", "ਗੁੰਠਾ");
        pa.put("katha", "ਕੱਠਾ");
        pa.put("decimal", "ਡੈਸੀਮਲ");
        pa.put("find_centers", "ਖੇਤੀਬਾੜੀ ਕੇਂਦਰ ਲੱਭੋ");
        pa.put("healthy", "ਸਿਹਤਮੰਦ");
        pa.put("detected", "ਪਛਾਣਿਆ ਗਿਆ ਰੋਗ");
        pa.put("click_for_info", "ਹੋਰ ਜਾਣਕਾਰੀ ਲਈ ਕਲਿੱਕ ਕਰੋ");
        pa.put("response_time", "ਜਵਾਬ ਸਮਾਂ: %.1f ਸਕਿੰਟ");
        pa.put("note_optional_date", "ਨੋਟ: ਮਿਤੀ ਚੋਣ ਵਿਕਲਪਿਕ ਹੈ।");
        pa.put("order", "ਤਰਤੀਬ");
        pa.put("avg_yield", "ਔਸਤ ਝਾੜ");
        pa.put("quintals", "ਕੁਇੰਟਲ");
        pa.put("quintals_acre", "ਕੁਇੰਟਲ/ਏਕੜ");
        pa.put("irrigation_interval", "ਸਿੰਚਾਈ ਦਾ ਵਕਫ਼ਾ");
        pa.put("days", "ਦਿਨ");
        pa.put("unit", "ਇਕਾਈ");
        pa.put("area", "ਰਕਬਾ");
        pa.put("calculate_requirements", "ਲੋੜ ਦੀ ਗਣਨਾ ਕਰੋ");
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
        String regional = (langMap != null) ? langMap.get(key) : null;

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
     * Formats an English string and regional string on separate lines without slashes.
     * If 'en' is chosen, returns english only.
     * Otherwise returns english + "\n" + regional.
     */
    public static String formatLines(Context context, String english, String regional) {
        if (english == null) return regional != null ? regional : "";
        String lang = getLanguage(context);
        if ("en".equalsIgnoreCase(lang)) {
            return english;
        }
        if (regional == null || regional.trim().isEmpty() || regional.equalsIgnoreCase(english)) {
            return english;
        }
        return english + "\n" + regional;
    }

    /**
     * Formats button text with English on line 1 and regional text on line 2 (no slashes).
     * If 'en' is chosen, returns english only.
     */
    public static String getButtonLines(Context context, String defaultEnglish, String key) {
        if (isEnglishOnly(context)) {
            return defaultEnglish;
        }
        String regional = getRegional(context, key, "");
        if (regional == null || regional.trim().isEmpty() || regional.equalsIgnoreCase(defaultEnglish)) {
            return defaultEnglish;
        }
        return defaultEnglish + "\n" + regional;
    }

    public static String getLocalizedCropName(Context context, String cropEn, String cropHi) {
        if (cropEn == null || cropEn.trim().isEmpty()) {
            return cropHi != null ? cropHi : "Crop";
        }
        return formatDual(context, cropEn.trim(), cropHi != null ? cropHi.trim() : null);
    }

    public static String getRegional(Context context, String key, String defaultEnglish) {
        String lang = getLanguage(context);
        if ("en".equalsIgnoreCase(lang)) {
            return defaultEnglish;
        }

        Map<String, String> langMap = DICTIONARY.get(lang.toLowerCase());
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
            new NamedItem("Chili", "मिर्च"),
            new NamedItem("Tomato", "टमाटर"),
            new NamedItem("Paddy", "धान"),
            new NamedItem("Millets", "बाजरा"),
            new NamedItem("Pulses", "दालें")
    };

    public static final NamedItem[] DEFAULT_STATES = {
            new NamedItem("Andhra Pradesh", "ఆంధ్రప్రదేశ్"),
            new NamedItem("Arunachal Pradesh", "अरुणाचल प्रदेश"),
            new NamedItem("Assam", "অসম"),
            new NamedItem("Bihar", "बिहार"),
            new NamedItem("Chhattisgarh", "छत्तीसगढ़"),
            new NamedItem("Goa", "गोवा"),
            new NamedItem("Gujarat", "ગુજરાત"),
            new NamedItem("Haryana", "हरियाणा"),
            new NamedItem("Himachal Pradesh", "हिमाचल प्रदेश"),
            new NamedItem("Jharkhand", "झारखंड"),
            new NamedItem("Karnataka", "ಕರ್ನಾಟಕ"),
            new NamedItem("Kerala", "കേരളം"),
            new NamedItem("Madhya Pradesh", "मध्य प्रदेश"),
            new NamedItem("Maharashtra", "महाराष्ट्र"),
            new NamedItem("Manipur", "मणिपुर"),
            new NamedItem("Meghalaya", "मेघालय"),
            new NamedItem("Mizoram", "मिजोरम"),
            new NamedItem("Nagaland", "नागालैंड"),
            new NamedItem("Odisha", "ওড়িশা"),
            new NamedItem("Punjab", "ਪੰਜਾਬ"),
            new NamedItem("Rajasthan", "राजस्थान"),
            new NamedItem("Sikkim", "सिक्किम"),
            new NamedItem("Tamil Nadu", "தமிழ்நாடு"),
            new NamedItem("Telangana", "తెలంగాణ"),
            new NamedItem("Tripura", "त्रिपुरा"),
            new NamedItem("Uttar Pradesh", "उत्तर प्रदेश"),
            new NamedItem("Uttarakhand", "उत्तराखंड"),
            new NamedItem("West Bengal", "পশ্চিমবঙ্গ"),
            new NamedItem("Delhi", "दिल्ली"),
            new NamedItem("Jammu & Kashmir", "जम्मू और कश्मीर"),
            new NamedItem("Ladakh", "लद्दाख")
    };

    public static String[] getLocalizedCrops(Context context) {
        String[] cropKeys = {
                "crop_wheat", "crop_rice", "crop_maize", "crop_cotton",
                "crop_sugarcane", "crop_soybean", "crop_mustard", "crop_potato",
                "crop_onion", "crop_chili", "crop_tomato", "crop_paddy",
                "crop_millets", "crop_pulses"
        };
        String[] cropEn = {
                "Wheat", "Rice", "Maize", "Cotton",
                "Sugarcane", "Soybean", "Mustard", "Potato",
                "Onion", "Chili", "Tomato", "Paddy",
                "Millets", "Pulses"
        };
        String[] result = new String[cropEn.length];
        for (int i = 0; i < cropEn.length; i++) {
            result[i] = getDual(context, cropEn[i], cropKeys[i]);
        }
        return result;
    }

    public static Map<String, Double> getRegionalUnits(Context context, String region) {
        Map<String, Double> universal = new HashMap<>();
        universal.put(getDual(context, "Acre", "acre"), 1.0);
        universal.put(getDual(context, "Hectare", "hectare"), 2.47);

        Map<String, Double> units = new HashMap<>(universal);
        if ("NORTH".equalsIgnoreCase(region)) {
            units.put(getDual(context, "Bigha", "bigha"), 0.625);
            units.put(getDual(context, "Kanal", "kanal"), 0.125);
            units.put(getDual(context, "Biswa", "biswa"), 0.031);
        } else if ("SOUTH".equalsIgnoreCase(region) || "WEST".equalsIgnoreCase(region)) {
            units.put(getDual(context, "Cent", "cent"), 0.01);
            units.put(getDual(context, "Guntha", "guntha"), 0.025);
        } else if ("EAST".equalsIgnoreCase(region)) {
            units.put(getDual(context, "Katha", "katha"), 0.031);
            units.put(getDual(context, "Decimal", "decimal"), 0.01);
        }
        return units;
    }

    public static String[] getLocalizedStates(Context context) {
        String[] result = new String[DEFAULT_STATES.length];
        for (int i = 0; i < DEFAULT_STATES.length; i++) {
            result[i] = formatDual(context, DEFAULT_STATES[i].en, DEFAULT_STATES[i].hi);
        }
        return result;
    }

    public static String[] getLocalizedSoils(Context context) {
        return new String[] {
                getDual(context, "Alluvial", "alluvial_soil"),
                getDual(context, "Black", "black_soil"),
                getDual(context, "Red", "red_soil"),
                getDual(context, "Laterite", "laterite_soil"),
                getDual(context, "Sandy", "sandy_soil"),
                getDual(context, "Clay", "clay_soil")
        };
    }

    public static String[] getLocalizedMethods(Context context) {
        return new String[] {
                getDual(context, "Flood", "flood_irrigation"),
                getDual(context, "Drip", "drip_irrigation"),
                getDual(context, "Sprinkler", "sprinkler_irrigation"),
                getDual(context, "Furrow", "furrow_irrigation"),
                getDual(context, "Basin", "basin_irrigation")
        };
    }

    public static String[] getLocalizedVarieties(Context context, String cropName) {
        String general = getDual(context, "General", "general_variety");
        String other = getDual(context, "Other", "other_variety");

        if (cropName == null) return new String[] { general, other };
        String lower = cropName.toLowerCase();

        if (lower.contains("wheat")) {
            return new String[] {
                    formatDual(context, "Lokwan", "लोकवन"),
                    formatDual(context, "Sharbati", "शरबती"),
                    formatDual(context, "Durum", "डुरम"),
                    formatDual(context, "Deshi", "देसी"),
                    other
            };
        } else if (lower.contains("rice") || lower.contains("paddy")) {
            return new String[] {
                    formatDual(context, "Basmati", "बासमती"),
                    formatDual(context, "Sona Masoori", "सोना मसूरी"),
                    formatDual(context, "Kolam", "कोलम"),
                    other
            };
        } else if (lower.contains("onion")) {
            return new String[] {
                    formatDual(context, "Red", "लाल"),
                    formatDual(context, "White", "सफेद"),
                    formatDual(context, "Pink", "गुलाबी"),
                    other
            };
        } else if (lower.contains("potato")) {
            return new String[] {
                    formatDual(context, "Jyoti", "ज्योति"),
                    formatDual(context, "Chandramukhi", "चंद्रमुखी"),
                    formatDual(context, "Sugar Free", "शुगर फ्री"),
                    other
            };
        } else if (lower.contains("tomato")) {
            return new String[] {
                    formatDual(context, "Hybrid", "हाइब्रिड"),
                    formatDual(context, "Deshi", "देसी"),
                    other
            };
        } else if (lower.contains("maize")) {
            return new String[] {
                    formatDual(context, "Yellow", "पीला"),
                    formatDual(context, "White", "सफेद"),
                    other
            };
        } else if (lower.contains("cotton")) {
            return new String[] {
                    formatDual(context, "American", "अमेरिकन"),
                    formatDual(context, "Deshi", "देसी"),
                    formatDual(context, "Bt Cotton", "बीटी कपास"),
                    other
            };
        } else if (lower.contains("mustard")) {
            return new String[] {
                    formatDual(context, "Black", "काली"),
                    formatDual(context, "Yellow", "पीली"),
                    other
            };
        } else if (lower.contains("soybean") || lower.contains("soyabean")) {
            return new String[] {
                    formatDual(context, "Yellow", "पीला"),
                    formatDual(context, "Black", "काला"),
                    other
            };
        }

        return new String[] { general, other };
    }

    public static String getLocalizedIrrigationStage(Context context, String cropName) {
        String lower = cropName != null ? cropName.toLowerCase() : "";
        String eng;
        String regionalKey;
        String fallbackHi;

        if (lower.contains("wheat")) {
            eng = "CRI Stage (21 days) & Flowering";
            regionalKey = "irrigation_stage_wheat";
            fallbackHi = "हर चरण (21 दिन) और फूल आने का समय";
        } else if (lower.contains("rice") || lower.contains("paddy")) {
            eng = "Tillering to Grain Filling";
            regionalKey = "irrigation_stage_rice";
            fallbackHi = "कल्ले फूटने से दाना भरने तक";
        } else if (lower.contains("sugarcane")) {
            eng = "Formative Stage (60-130 Days)";
            regionalKey = "irrigation_stage_sugarcane";
            fallbackHi = "प्रारंभिक विकास चरण (60-130 दिन)";
        } else if (lower.contains("potato")) {
            eng = "Stolon formation & Tuberization";
            regionalKey = "irrigation_stage_potato";
            fallbackHi = "स्टोलन बनना और कंद बनना";
        } else {
            eng = "Active Vegetative Growth";
            regionalKey = "irrigation_stage_general";
            fallbackHi = "सक्रिय वानस्पतिक विकास का चरण";
        }

        if (isEnglishOnly(context)) {
            return eng + ".";
        }
        String reg = getRegional(context, regionalKey, fallbackHi);
        return eng + ".\n" + reg + "।";
    }

    public static String getLocalizedIrrigationTip(Context context, String cropName) {
        String lower = cropName != null ? cropName.toLowerCase() : "";
        String eng;
        String regionalKey;
        String fallbackHi;

        if (lower.contains("wheat")) {
            eng = "Avoid water stress during the CRI stage to ensure root growth";
            regionalKey = "irrigation_tip_wheat";
            fallbackHi = "जड़ों के विकास के लिए सीआरआई चरण के दौरान पानी की कमी से बचें";
        } else if (lower.contains("rice") || lower.contains("paddy")) {
            eng = "Keep 5cm water level for flood method";
            regionalKey = "irrigation_tip_rice";
            fallbackHi = "सतह सिंचाई के लिए 5 सेमी पानी का स्तर बनाए रखें";
        } else if (lower.contains("sugarcane")) {
            eng = "Deep irrigation is required for long intervals";
            regionalKey = "irrigation_tip_sugarcane";
            fallbackHi = "लंबे अंतराल के लिए गहरी सिंचाई आवश्यक है";
        } else if (lower.contains("potato")) {
            eng = "Light but frequent irrigation is best for tubers";
            regionalKey = "irrigation_tip_potato";
            fallbackHi = "कंदों के लिए हल्की लेकिन बार-बार सिंचाई सबसे अच्छी होती है";
        } else {
            eng = "Monitor soil moisture at 15cm depth";
            regionalKey = "irrigation_tip_general";
            fallbackHi = "15 सेमी की गहराई पर मिट्टी की नमी की निगरानी करें";
        }

        if (isEnglishOnly(context)) {
            return eng + ".";
        }
        String reg = getRegional(context, regionalKey, fallbackHi);
        return eng + ".\n" + reg + "।";
    }

    public static String getLocalizedWeatherDesc(Context context, String descEng) {
        if (descEng == null || descEng.trim().isEmpty()) return "";
        String clean = descEng.trim();
        String capitalized = clean.substring(0, 1).toUpperCase() + clean.substring(1);
        if (isEnglishOnly(context)) {
            return capitalized;
        }

        Map<String, String> weatherTranslations = new HashMap<>();
        String lang = getLanguage(context).toLowerCase();
        if ("hi".equals(lang)) {
            weatherTranslations.put("clear sky", "साफ आसमान");
            weatherTranslations.put("few clouds", "थोड़े बादल");
            weatherTranslations.put("scattered clouds", "छितरे बादल");
            weatherTranslations.put("broken clouds", "टूटे बादल");
            weatherTranslations.put("overcast clouds", "बादलों से ढका");
            weatherTranslations.put("shower rain", "तेज़ बारिश");
            weatherTranslations.put("rain", "बारिश");
            weatherTranslations.put("light rain", "हल्की बारिश");
            weatherTranslations.put("moderate rain", "मध्यम बारिश");
            weatherTranslations.put("thunderstorm", "आंधी-तूफान");
            weatherTranslations.put("snow", "बर्फबारी");
            weatherTranslations.put("mist", "धुंध");
            weatherTranslations.put("haze", "धुंध");
        } else if ("bn".equals(lang)) {
            weatherTranslations.put("clear sky", "পরিষ্কার আকাশ");
            weatherTranslations.put("few clouds", "অল্প মেঘ");
            weatherTranslations.put("scattered clouds", "বিক্ষিপ্ত মেঘ");
            weatherTranslations.put("broken clouds", "ভাঙা মেঘ");
            weatherTranslations.put("overcast clouds", "মেঘলা আকাশ");
            weatherTranslations.put("shower rain", "ঝিরিঝিরি বৃষ্টি");
            weatherTranslations.put("rain", "বৃষ্টি");
            weatherTranslations.put("light rain", "হালকা বৃষ্টি");
            weatherTranslations.put("moderate rain", "মাঝারি বৃষ্টি");
            weatherTranslations.put("thunderstorm", "বজ্রপাতসহ ঝড়");
            weatherTranslations.put("snow", "তুষারপাত");
            weatherTranslations.put("mist", "কুয়াশা");
            weatherTranslations.put("haze", "ধোঁয়াশা");
        } else if ("te".equals(lang)) {
            weatherTranslations.put("clear sky", "నిర్మలమైన ఆకాశం");
            weatherTranslations.put("few clouds", "కొద్దిగా మేఘాలు");
            weatherTranslations.put("scattered clouds", "చెల్లాచెదురుగా మేఘాలు");
            weatherTranslations.put("broken clouds", "విరిగిన మేఘాలు");
            weatherTranslations.put("overcast clouds", "పూర్తిగా మేఘావృతం");
            weatherTranslations.put("shower rain", "జల్లులు");
            weatherTranslations.put("rain", "వర్షం");
            weatherTranslations.put("light rain", "తేలికపాటి వర్షం");
            weatherTranslations.put("moderate rain", "మధ్యస్థ వర్షం");
            weatherTranslations.put("thunderstorm", "ఉరుములతో కూడిన వర్షం");
            weatherTranslations.put("snow", "మంచు");
            weatherTranslations.put("mist", "పొగమంచు");
            weatherTranslations.put("haze", "మసక");
        } else if ("mr".equals(lang)) {
            weatherTranslations.put("clear sky", "स्वच्छ आकाश");
            weatherTranslations.put("few clouds", "थोडे ढग");
            weatherTranslations.put("scattered clouds", "विखुरलेले ढग");
            weatherTranslations.put("broken clouds", "तुटक ढग");
            weatherTranslations.put("overcast clouds", "ढगाळ वातावरण");
            weatherTranslations.put("shower rain", "मुसळधार पाऊस");
            weatherTranslations.put("rain", "पाऊस");
            weatherTranslations.put("light rain", "हलका पाऊस");
            weatherTranslations.put("moderate rain", "मध्यम पाऊस");
            weatherTranslations.put("thunderstorm", "वादळी पाऊस");
            weatherTranslations.put("snow", "बर्फवृष्टी");
            weatherTranslations.put("mist", "धुके");
            weatherTranslations.put("haze", "धुरकट");
        } else if ("ta".equals(lang)) {
            weatherTranslations.put("clear sky", "தெளிவான வானம்");
            weatherTranslations.put("few clouds", "சில மேகங்கள்");
            weatherTranslations.put("scattered clouds", "சிதறிய மேகங்கள்");
            weatherTranslations.put("broken clouds", "உடைந்த மேகங்கள்");
            weatherTranslations.put("overcast clouds", "மேகமூட்டம்");
            weatherTranslations.put("shower rain", "மழை தூறல்");
            weatherTranslations.put("rain", "மழை");
            weatherTranslations.put("light rain", "லேசான மழை");
            weatherTranslations.put("moderate rain", "மிதமான மழை");
            weatherTranslations.put("thunderstorm", "இடியுடன் கூடிய மழை");
            weatherTranslations.put("snow", "பனிப்பொழிவு");
            weatherTranslations.put("mist", "பனிமூட்டம்");
            weatherTranslations.put("haze", "புகைமூட்டம்");
        } else if ("gu".equals(lang)) {
            weatherTranslations.put("clear sky", "સ્વચ્છ આકાશ");
            weatherTranslations.put("few clouds", "થોડા વાદળો");
            weatherTranslations.put("scattered clouds", "છૂટાછવાયા વાદળો");
            weatherTranslations.put("broken clouds", "તૂટેલા વાદળો");
            weatherTranslations.put("overcast clouds", "વાદળછાયું આકાશ");
            weatherTranslations.put("shower rain", "ઝાપટાં");
            weatherTranslations.put("rain", "વરસાદ");
            weatherTranslations.put("light rain", "હળવો વરસાદ");
            weatherTranslations.put("moderate rain", "મધ્યમ વરસાદ");
            weatherTranslations.put("thunderstorm", "ગાજવીજ સાથે વરસાદ");
            weatherTranslations.put("snow", "બરફવર્ષા");
            weatherTranslations.put("mist", "ધુમ્મસ");
            weatherTranslations.put("haze", "ઝાકળ");
        } else if ("kn".equals(lang)) {
            weatherTranslations.put("clear sky", "ಸ್ವಚ್ಛ ಆಕಾಶ");
            weatherTranslations.put("few clouds", "ಕೆಲವು ಮೋಡಗಳು");
            weatherTranslations.put("scattered clouds", "ಚದುರಿದ ಮೋಡಗಳು");
            weatherTranslations.put("broken clouds", "ಮುರಿದ ಮೋಡಗಳು");
            weatherTranslations.put("overcast clouds", "ಮೋಡ ಕವಿದ ಆಕಾಶ");
            weatherTranslations.put("shower rain", "ಮಳೆ ಹನಿಗಳು");
            weatherTranslations.put("rain", "ಮಳೆ");
            weatherTranslations.put("light rain", "ಹಗುರ ಮಳೆ");
            weatherTranslations.put("moderate rain", "ಮಧ್ಯಮ ಮಳೆ");
            weatherTranslations.put("thunderstorm", "ಗುಡುಗು ಸಹಿತ ಮಳೆ");
            weatherTranslations.put("snow", "ಹಿಮಪಾತ");
            weatherTranslations.put("mist", "ಮಂಜು");
            weatherTranslations.put("haze", "ದಟ್ಟ ಮಂಜು");
        } else if ("ml".equals(lang)) {
            weatherTranslations.put("clear sky", "തെളിഞ്ഞ ആകാശം");
            weatherTranslations.put("few clouds", "കുറച്ചു മേഘങ്ങൾ");
            weatherTranslations.put("scattered clouds", "ചിതറിയ മേഘങ്ങൾ");
            weatherTranslations.put("broken clouds", "മേഘാവൃതം");
            weatherTranslations.put("overcast clouds", "മൂടിയ ആകാശം");
            weatherTranslations.put("shower rain", "ശക്തമായ മഴ");
            weatherTranslations.put("rain", "മഴ");
            weatherTranslations.put("light rain", "നേരിയ മഴ");
            weatherTranslations.put("moderate rain", "മിതമായ മഴ");
            weatherTranslations.put("thunderstorm", "ഇടിമിന്നലോടു കൂടിയ മഴ");
            weatherTranslations.put("snow", "മഞ്ഞുവീഴ്ച");
            weatherTranslations.put("mist", "മൂടൽമഞ്ഞ്");
            weatherTranslations.put("haze", "ധൂമം");
        } else if ("pa".equals(lang)) {
            weatherTranslations.put("clear sky", "ਸਾਫ਼ ਅਸਮਾਨ");
            weatherTranslations.put("few clouds", "ਥੋੜ੍ਹੇ ਬੱਦਲ");
            weatherTranslations.put("scattered clouds", "ਖਿੰਡੇ ਹੋਏ ਬੱਦਲ");
            weatherTranslations.put("broken clouds", "ਟੁੱਟੇ ਬੱਦਲ");
            weatherTranslations.put("overcast clouds", "ਬੱਦਲਵਾਈ");
            weatherTranslations.put("shower rain", "ਤੇਜ਼ ਮੀਂਹ");
            weatherTranslations.put("rain", "ਮੀਂਹ");
            weatherTranslations.put("light rain", "ਹਲਕਾ ਮੀਂਹ");
            weatherTranslations.put("moderate rain", "ਦਰਮਿਆਨਾ ਮੀਂਹ");
            weatherTranslations.put("thunderstorm", "ਤੂਫ਼ਾਨ ਨਾਲ ਮੀਂਹ");
            weatherTranslations.put("snow", "ਬਰਫ਼ਬਾਰੀ");
            weatherTranslations.put("mist", "ਧੁੰਦ");
            weatherTranslations.put("haze", "ਧੁੰਦ");
        }

        String reg = weatherTranslations.get(clean.toLowerCase());
        if (reg != null && !reg.trim().isEmpty()) {
            return capitalized + " / " + reg;
        }
        return capitalized;
    }
}
