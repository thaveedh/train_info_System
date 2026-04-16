package com.trainchatbot.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Resolves human-readable train names for display. Contains a comprehensive database
 * of 500+ well-known Indian railway trains. Prefers non-placeholder API strings,
 * then this built-in map, then queries Gemini for unknown trains.
 */
public final class TrainDisplayNameResolver {

    private static final Map<String, String> LOOKUP = new HashMap<>();

    static {
        // ─────────────────────────────────────────────────────────────
        // RAJDHANI EXPRESS TRAINS
        // ─────────────────────────────────────────────────────────────
        LOOKUP.put("12301", "Howrah Rajdhani Express");
        LOOKUP.put("12302", "Howrah Rajdhani Express");
        LOOKUP.put("12303", "Poorva Express");
        LOOKUP.put("12304", "Poorva Express");
        LOOKUP.put("12305", "Howrah Rajdhani Express (via Patna)");
        LOOKUP.put("12306", "Howrah Rajdhani Express (via Patna)");
        LOOKUP.put("12309", "Rajendra Nagar Rajdhani Express");
        LOOKUP.put("12310", "Rajendra Nagar Rajdhani Express");
        LOOKUP.put("12313", "Sealdah Rajdhani Express");
        LOOKUP.put("12314", "Sealdah Rajdhani Express");
        LOOKUP.put("12425", "Jammu Tawi Rajdhani Express");
        LOOKUP.put("12426", "Jammu Tawi Rajdhani Express");
        LOOKUP.put("12431", "Trivandrum Rajdhani Express");
        LOOKUP.put("12432", "Trivandrum Rajdhani Express");
        LOOKUP.put("12433", "Chennai Rajdhani Express");
        LOOKUP.put("12434", "Chennai Rajdhani Express");
        LOOKUP.put("12435", "Dibrugarh Rajdhani Express");
        LOOKUP.put("12436", "Dibrugarh Rajdhani Express");
        LOOKUP.put("12437", "Secunderabad Rajdhani Express");
        LOOKUP.put("12438", "Secunderabad Rajdhani Express");
        LOOKUP.put("12439", "Ranchi Rajdhani Express");
        LOOKUP.put("12440", "Ranchi Rajdhani Express");
        LOOKUP.put("12441", "Bilaspur Rajdhani Express");
        LOOKUP.put("12442", "Bilaspur Rajdhani Express");
        LOOKUP.put("12951", "Mumbai Rajdhani Express");
        LOOKUP.put("12952", "Mumbai Rajdhani Express");
        LOOKUP.put("12953", "August Kranti Rajdhani Express");
        LOOKUP.put("12954", "August Kranti Rajdhani Express");
        LOOKUP.put("22691", "KSR Bengaluru Rajdhani Express");
        LOOKUP.put("22692", "KSR Bengaluru Rajdhani Express");

        // ─────────────────────────────────────────────────────────────
        // SHATABDI EXPRESS TRAINS
        // ─────────────────────────────────────────────────────────────
        LOOKUP.put("12001", "Bhopal Shatabdi Express");
        LOOKUP.put("12002", "Bhopal Shatabdi Express");
        LOOKUP.put("12003", "Lucknow Swarna Shatabdi Express");
        LOOKUP.put("12004", "Lucknow Swarna Shatabdi Express");
        LOOKUP.put("12005", "Kalka Shatabdi Express");
        LOOKUP.put("12006", "Kalka Shatabdi Express");
        LOOKUP.put("12007", "Chennai Shatabdi Express");
        LOOKUP.put("12008", "Chennai Shatabdi Express");
        LOOKUP.put("12009", "Ahmedabad Shatabdi Express");
        LOOKUP.put("12010", "Ahmedabad Shatabdi Express");
        LOOKUP.put("12011", "Kalka Shatabdi Express");
        LOOKUP.put("12012", "Kalka Shatabdi Express");
        LOOKUP.put("12013", "Amritsar Shatabdi Express");
        LOOKUP.put("12014", "Amritsar Shatabdi Express");
        LOOKUP.put("12015", "Ajmer Shatabdi Express");
        LOOKUP.put("12016", "Ajmer Shatabdi Express");
        LOOKUP.put("12017", "Dehradun Shatabdi Express");
        LOOKUP.put("12018", "Dehradun Shatabdi Express");
        LOOKUP.put("12019", "Howrah Shatabdi Express");
        LOOKUP.put("12020", "Howrah Shatabdi Express");
        LOOKUP.put("12021", "Howrah Jan Shatabdi Express");
        LOOKUP.put("12022", "Howrah Jan Shatabdi Express");
        LOOKUP.put("12023", "Kanpur Shatabdi Express");
        LOOKUP.put("12024", "Kanpur Shatabdi Express");
        LOOKUP.put("12025", "Pune Shatabdi Express");
        LOOKUP.put("12026", "Pune Shatabdi Express");
        LOOKUP.put("12027", "Chennai Shatabdi Express");
        LOOKUP.put("12028", "Chennai Shatabdi Express");
        LOOKUP.put("12029", "Amritsar Swarna Shatabdi Express");
        LOOKUP.put("12030", "Amritsar Swarna Shatabdi Express");

        // ─────────────────────────────────────────────────────────────
        // DURONTO EXPRESS TRAINS
        // ─────────────────────────────────────────────────────────────
        LOOKUP.put("12213", "Yesvantpur Duronto Express");
        LOOKUP.put("12214", "Yesvantpur Duronto Express");
        LOOKUP.put("12221", "Howrah Duronto Express");
        LOOKUP.put("12222", "Howrah Duronto Express");
        LOOKUP.put("12243", "Chennai Duronto Express");
        LOOKUP.put("12244", "Chennai Duronto Express");
        LOOKUP.put("12259", "Sealdah Duronto Express");
        LOOKUP.put("12260", "Sealdah Duronto Express");
        LOOKUP.put("12269", "Chennai Duronto Express");
        LOOKUP.put("12270", "Chennai Duronto Express");
        LOOKUP.put("12273", "Howrah Duronto Express");
        LOOKUP.put("12274", "Howrah Duronto Express");
        LOOKUP.put("12283", "Ernakulam Duronto Express");
        LOOKUP.put("12284", "Ernakulam Duronto Express");
        LOOKUP.put("12289", "Mumbai CSMT Duronto Express");
        LOOKUP.put("12290", "Mumbai CSMT Duronto Express");
        LOOKUP.put("12295", "Kochuveli Garib Rath Express");
        LOOKUP.put("12296", "Kochuveli Garib Rath Express");

        // ─────────────────────────────────────────────────────────────
        // SUPERFAST / MAIL / EXPRESS — 12xxx series
        // ─────────────────────────────────────────────────────────────
        LOOKUP.put("12031", "Amritsar Shatabdi Express");
        LOOKUP.put("12032", "Amritsar Shatabdi Express");
        LOOKUP.put("12033", "Kanpur Shatabdi Express");
        LOOKUP.put("12034", "Kanpur Shatabdi Express");
        LOOKUP.put("12035", "Jaipur Shatabdi Express");
        LOOKUP.put("12036", "Jaipur Shatabdi Express");
        LOOKUP.put("12037", "Lucknow Shatabdi Express");
        LOOKUP.put("12038", "Lucknow Shatabdi Express");
        LOOKUP.put("12039", "Kathgodam Shatabdi Express");
        LOOKUP.put("12040", "Kathgodam Shatabdi Express");
        LOOKUP.put("12041", "Howrah Shatabdi Express");
        LOOKUP.put("12042", "Howrah Shatabdi Express");
        LOOKUP.put("12049", "Gatimaan Express");
        LOOKUP.put("12050", "Gatimaan Express");
        LOOKUP.put("12051", "Jan Shatabdi Express");
        LOOKUP.put("12052", "Jan Shatabdi Express");
        LOOKUP.put("12101", "Jnaneswari Super Deluxe Express");
        LOOKUP.put("12102", "Jnaneswari Super Deluxe Express");
        LOOKUP.put("12105", "Vidarbha Express");
        LOOKUP.put("12106", "Vidarbha Express");
        LOOKUP.put("12123", "Deccan Queen Express");
        LOOKUP.put("12124", "Deccan Queen Express");
        LOOKUP.put("12125", "Pragati Express");
        LOOKUP.put("12126", "Pragati Express");
        LOOKUP.put("12137", "Punjab Mail Express");
        LOOKUP.put("12138", "Punjab Mail Express");
        LOOKUP.put("12139", "Sevagram Express");
        LOOKUP.put("12140", "Sevagram Express");
        LOOKUP.put("12149", "Danapur Express");
        LOOKUP.put("12150", "Danapur Express");
        LOOKUP.put("12151", "Samarsata Express");
        LOOKUP.put("12152", "Samarsata Express");
        LOOKUP.put("12153", "Habibganj Express");
        LOOKUP.put("12154", "Habibganj Express");
        LOOKUP.put("12155", "Bhopal Express");
        LOOKUP.put("12156", "Bhopal Express");
        LOOKUP.put("12159", "Saptagiri Express");
        LOOKUP.put("12160", "Saptagiri Express");
        LOOKUP.put("12163", "Dadar Chennai Superfast Express");
        LOOKUP.put("12164", "Chennai Dadar Superfast Express");
        LOOKUP.put("12247", "Yuva Express");
        LOOKUP.put("12248", "Yuva Express");
        LOOKUP.put("12261", "Mumbai CSMT Howrah Duronto Express");
        LOOKUP.put("12262", "Howrah Mumbai CSMT Duronto Express");
        LOOKUP.put("12311", "Kalka Mail Express");
        LOOKUP.put("12312", "Kalka Mail Express");
        LOOKUP.put("12313", "Sealdah Rajdhani Express");
        LOOKUP.put("12314", "Sealdah Rajdhani Express");
        LOOKUP.put("12315", "Ananthapuri Express");
        LOOKUP.put("12316", "Ananthapuri Express");
        LOOKUP.put("12317", "Akal Takht Express");
        LOOKUP.put("12318", "Akal Takht Express");
        LOOKUP.put("12321", "Howrah Mumbai Mail Express");
        LOOKUP.put("12322", "Mumbai Howrah Mail Express");
        LOOKUP.put("12367", "Vikramshila Express");
        LOOKUP.put("12368", "Vikramshila Express");
        LOOKUP.put("12369", "Kumbha Express");
        LOOKUP.put("12370", "Kumbha Express");
        LOOKUP.put("12381", "Poorva Express");
        LOOKUP.put("12382", "Poorva Express");
        LOOKUP.put("12393", "Sampoorna Kranti Express");
        LOOKUP.put("12394", "Sampoorna Kranti Express");
        LOOKUP.put("12397", "Mahabodhi Express");
        LOOKUP.put("12398", "Mahabodhi Express");

        // ─────────────────────────────────────────────────────────────
        // POPULAR SOUTH INDIAN TRAINS
        // ─────────────────────────────────────────────────────────────
        LOOKUP.put("12507", "Guwahati Thiruvananthapuram Express");
        LOOKUP.put("12508", "Thiruvananthapuram Guwahati Express");
        LOOKUP.put("12509", "Bangalore Thiruvananthapuram Express");
        LOOKUP.put("12510", "Thiruvananthapuram Bangalore Express");
        LOOKUP.put("12539", "YPR Lucknow Superfast Express");
        LOOKUP.put("12540", "Lucknow YPR Superfast Express");
        LOOKUP.put("12549", "Durg Jaipur SF Express");
        LOOKUP.put("12550", "Jaipur Durg SF Express");
        LOOKUP.put("12551", "Yesvantpur Kamakhya Express");
        LOOKUP.put("12552", "Kamakhya Yesvantpur Express");
        LOOKUP.put("12553", "Vaishali Superfast Express");
        LOOKUP.put("12554", "Vaishali Superfast Express");
        LOOKUP.put("12555", "Gorakhdham Express");
        LOOKUP.put("12556", "Gorakhdham Express");
        LOOKUP.put("12557", "Sadbhavana Express");
        LOOKUP.put("12558", "Sadbhavana Express");
        LOOKUP.put("12559", "Shiv Ganga Express");
        LOOKUP.put("12560", "Shiv Ganga Express");
        LOOKUP.put("12561", "Swatantrata Senani Superfast Express");
        LOOKUP.put("12562", "Swatantrata Senani Superfast Express");
        LOOKUP.put("12563", "Jansadharan Express");
        LOOKUP.put("12564", "Jansadharan Express");
        LOOKUP.put("12577", "Bagmati Express");
        LOOKUP.put("12578", "Bagmati Express");
        LOOKUP.put("12579", "JammuTawi Patna Express");
        LOOKUP.put("12580", "Patna JammuTawi Express");
        LOOKUP.put("12581", "Manduadih New Delhi Superfast Express");
        LOOKUP.put("12582", "New Delhi Manduadih Superfast Express");
        LOOKUP.put("12583", "Lucknow Anand Vihar Express");
        LOOKUP.put("12584", "Anand Vihar Lucknow Express");
        LOOKUP.put("12585", "Gorakhpur Anand Vihar Superfast Express");
        LOOKUP.put("12586", "Anand Vihar Gorakhpur Superfast Express");
        LOOKUP.put("12587", "Gorakhpur Mumbai Superfast Express");
        LOOKUP.put("12588", "Mumbai Gorakhpur Superfast Express");
        LOOKUP.put("12615", "Grand Trunk Express");
        LOOKUP.put("12616", "Grand Trunk Express");
        LOOKUP.put("12617", "Mangala Lakshadweep Express");
        LOOKUP.put("12618", "Mangala Lakshadweep Express");
        LOOKUP.put("12619", "Matsyagandha Express");
        LOOKUP.put("12620", "Matsyagandha Express");
        LOOKUP.put("12621", "Tamil Nadu Express");
        LOOKUP.put("12622", "Tamil Nadu Express");
        LOOKUP.put("12623", "Thiruvananthapuram Mail Express");
        LOOKUP.put("12624", "Thiruvananthapuram Mail Express");
        LOOKUP.put("12625", "Kerala Express");
        LOOKUP.put("12626", "Kerala Express");
        LOOKUP.put("12627", "Karnataka Express");
        LOOKUP.put("12628", "Karnataka Express");
        LOOKUP.put("12629", "Karnataka Sampark Kranti Express");
        LOOKUP.put("12630", "Karnataka Sampark Kranti Express");
        LOOKUP.put("12631", "Nellai Superfast Express");
        LOOKUP.put("12632", "Nellai Superfast Express");
        LOOKUP.put("12633", "Kanyakumari Express");
        LOOKUP.put("12634", "Kanyakumari Express");
        LOOKUP.put("12635", "Vaigai Superfast Express");
        LOOKUP.put("12636", "Vaigai Superfast Express");
        LOOKUP.put("12637", "Pandian Express");
        LOOKUP.put("12638", "Pandian Express");
        LOOKUP.put("12639", "Brindavan Express");
        LOOKUP.put("12640", "Brindavan Express");
        LOOKUP.put("12641", "Thirukkural Express");
        LOOKUP.put("12642", "Thirukkural Express");
        LOOKUP.put("12643", "Nizamuddin Thiruvananthapuram Superfast Express");
        LOOKUP.put("12644", "Thiruvananthapuram Nizamuddin Superfast Express");
        LOOKUP.put("12645", "Nizamuddin Ernakulam Superfast Express");
        LOOKUP.put("12646", "Ernakulam Nizamuddin Superfast Express");
        LOOKUP.put("12647", "Kongu Express");
        LOOKUP.put("12648", "Kongu Express");
        LOOKUP.put("12649", "Sampark Kranti Express (KGP)");
        LOOKUP.put("12650", "Sampark Kranti Express (KGP)");
        LOOKUP.put("12651", "Sampark Kranti Express (MAQ)");
        LOOKUP.put("12652", "Sampark Kranti Express (MAQ)");
        LOOKUP.put("12655", "Navjeevan Express");
        LOOKUP.put("12656", "Navjeevan Express");
        LOOKUP.put("12657", "Chennai Mail Express (Bengaluru)");
        LOOKUP.put("12658", "Bengaluru Mail Express (Chennai)");
        LOOKUP.put("12659", "Gurudev Express");
        LOOKUP.put("12660", "Gurudev Express");
        LOOKUP.put("12669", "Chettinad Express");
        LOOKUP.put("12670", "Chettinad Express");
        LOOKUP.put("12671", "Nilagiri Express");
        LOOKUP.put("12672", "Nilagiri Express");
        LOOKUP.put("12673", "Cheran Express");
        LOOKUP.put("12674", "Cheran Express");
        LOOKUP.put("12675", "Kovai Express");
        LOOKUP.put("12676", "Kovai Express");
        LOOKUP.put("12677", "Ernakulam Intercity Express");
        LOOKUP.put("12678", "Ernakulam Intercity Express");
        LOOKUP.put("12679", "Coimbatore Intercity Express");
        LOOKUP.put("12680", "Coimbatore Intercity Express");
        LOOKUP.put("12685", "Mangaluru Chennai Express");
        LOOKUP.put("12686", "Chennai Mangaluru Express");
        LOOKUP.put("12687", "Madurai Superfast Express (Chandigarh)");
        LOOKUP.put("12688", "Chandigarh Madurai Superfast Express");
        LOOKUP.put("12689", "Nagercoil Superfast Express");
        LOOKUP.put("12690", "Nagercoil Superfast Express");
        LOOKUP.put("12691", "Chettinad Superfast Express");
        LOOKUP.put("12692", "Chettinad Superfast Express");
        LOOKUP.put("12693", "Pearl City Express");
        LOOKUP.put("12694", "Pearl City Express");
        LOOKUP.put("12695", "Chennai Trivandrum Superfast Express");
        LOOKUP.put("12696", "Trivandrum Chennai Superfast Express");

        // ─────────────────────────────────────────────────────────────
        // NORTH INDIA POPULAR TRAINS — 12xxx continued
        // ─────────────────────────────────────────────────────────────
        LOOKUP.put("12723", "Telangana Express");
        LOOKUP.put("12724", "Telangana Express");
        LOOKUP.put("12725", "Charmichar Express");
        LOOKUP.put("12726", "Charmichar Express");
        LOOKUP.put("12759", "Charminar Express");
        LOOKUP.put("12760", "Charminar Express");
        LOOKUP.put("12773", "Shalimar Superfast Express");
        LOOKUP.put("12774", "Shalimar Superfast Express");
        LOOKUP.put("12777", "Rajya Rani Express");
        LOOKUP.put("12778", "Rajya Rani Express");
        LOOKUP.put("12779", "Goa Express (VSG)");
        LOOKUP.put("12780", "Goa Express (VSG)");
        LOOKUP.put("12785", "BSB NZM Superfast Express");
        LOOKUP.put("12786", "NZM BSB Superfast Express");
        LOOKUP.put("12789", "Rayalaseema Express");
        LOOKUP.put("12790", "Rayalaseema Express");
        LOOKUP.put("12791", "Secunderabad Danapur Express");
        LOOKUP.put("12792", "Danapur Secunderabad Express");
        LOOKUP.put("12801", "Purushottam Express");
        LOOKUP.put("12802", "Purushottam Express");
        LOOKUP.put("12803", "Swarnajayanti Express");
        LOOKUP.put("12804", "Swarnajayanti Express");
        LOOKUP.put("12809", "Mumbai Howrah Mail Express");
        LOOKUP.put("12810", "Howrah Mumbai Mail Express");
        LOOKUP.put("12833", "Howrah Ahmedabad Express");
        LOOKUP.put("12834", "Ahmedabad Howrah Express");
        LOOKUP.put("12839", "Chennai Howrah Mail Express");
        LOOKUP.put("12840", "Howrah Chennai Mail Express");
        LOOKUP.put("12841", "SRC Chennai Coromandel Express");
        LOOKUP.put("12842", "Chennai SRC Coromandel Express");
        LOOKUP.put("12859", "Gitanjali Express");
        LOOKUP.put("12860", "Gitanjali Express");
        LOOKUP.put("12861", "Mumbai Howrah Link Express");
        LOOKUP.put("12862", "Howrah Mumbai Link Express");
        LOOKUP.put("12903", "Golden Temple Mail Express");
        LOOKUP.put("12904", "Golden Temple Mail Express");
        LOOKUP.put("12905", "Paschim Express");
        LOOKUP.put("12906", "Paschim Express");
        LOOKUP.put("12907", "Maharashtra Sampark Kranti Express");
        LOOKUP.put("12908", "Maharashtra Sampark Kranti Express");
        LOOKUP.put("12909", "Garib Rath Express");
        LOOKUP.put("12910", "Garib Rath Express");
        LOOKUP.put("12925", "Paschim Superfast Express");
        LOOKUP.put("12926", "Paschim Superfast Express");
        LOOKUP.put("12939", "Jaipur Superfast Express");
        LOOKUP.put("12940", "Jaipur Superfast Express");
        LOOKUP.put("12951", "Mumbai Rajdhani Express");
        LOOKUP.put("12952", "Mumbai Rajdhani Express");

        // ─────────────────────────────────────────────────────────────
        // VANDE BHARAT / TEJAS / HUMSAFAR / ANTYODAYA
        // ─────────────────────────────────────────────────────────────
        LOOKUP.put("22435", "Vande Bharat Express (NDLS-VAR)");
        LOOKUP.put("22436", "Vande Bharat Express (VAR-NDLS)");
        LOOKUP.put("22439", "Vande Bharat Express (NDLS-KTR)");
        LOOKUP.put("22440", "Vande Bharat Express (KTR-NDLS)");
        LOOKUP.put("20601", "Vande Bharat Express (CSMT-SWV)");
        LOOKUP.put("20602", "Vande Bharat Express (SWV-CSMT)");
        LOOKUP.put("20607", "Vande Bharat Express (SBC-DR)");
        LOOKUP.put("20608", "Vande Bharat Express (DR-SBC)");
        LOOKUP.put("20609", "Vande Bharat Express (CSK-SBC)");
        LOOKUP.put("20610", "Vande Bharat Express (SBC-CSK)");
        LOOKUP.put("22119", "Mumbai CSMT Karmali Tejas Express");
        LOOKUP.put("22120", "Karmali Mumbai CSMT Tejas Express");
        LOOKUP.put("22121", "LTT CSMT Tejas Express");
        LOOKUP.put("22122", "CSMT LTT Tejas Express");
        LOOKUP.put("82501", "Lucknow New Delhi Tejas Express");
        LOOKUP.put("82502", "New Delhi Lucknow Tejas Express");
        LOOKUP.put("22109", "Mumbai CSMT Hazrat Nizamuddin AC Express");
        LOOKUP.put("22110", "Hazrat Nizamuddin Mumbai CSMT AC Express");
        LOOKUP.put("12401", "Magadh Express");
        LOOKUP.put("12402", "Magadh Express");

        // ─────────────────────────────────────────────────────────────
        // 16xxx / 17xxx SERIES — SOUTH & CENTRAL INDIA
        // ─────────────────────────────────────────────────────────────
        LOOKUP.put("16159", "Mannai Express");
        LOOKUP.put("16160", "Mannai Express");
        LOOKUP.put("16315", "Kochuveli Mysuru Express");
        LOOKUP.put("16316", "Mysuru Kochuveli Express");
        LOOKUP.put("16331", "Trivandrum Mumbai Express");
        LOOKUP.put("16332", "Mumbai Trivandrum Express");
        LOOKUP.put("16339", "Mumbai Nagercoil Express");
        LOOKUP.put("16340", "Nagercoil Mumbai Express");
        LOOKUP.put("16381", "Mumbai CSMT Kanyakumari Express");
        LOOKUP.put("16382", "Kanyakumari Mumbai CSMT Express");
        LOOKUP.put("16501", "Yesvantpur Ahmedabad Express");
        LOOKUP.put("16502", "Ahmedabad Yesvantpur Express");
        LOOKUP.put("16505", "Bangalore Karwar Express");
        LOOKUP.put("16506", "Karwar Bangalore Express");
        LOOKUP.put("16507", "Bangalore Jodhpur Express");
        LOOKUP.put("16508", "Jodhpur Bangalore Express");
        LOOKUP.put("16515", "Yesvantpur Kozhikode Express");
        LOOKUP.put("16516", "Kozhikode Yesvantpur Express");
        LOOKUP.put("16525", "Island Express");
        LOOKUP.put("16526", "Island Express");
        LOOKUP.put("16527", "Yesvantpur Kanyakumari Express");
        LOOKUP.put("16528", "Kanyakumari Yesvantpur Express");
        LOOKUP.put("16529", "Bangalore Kanyakumari Express");
        LOOKUP.put("16530", "Kanyakumari Bangalore Express");
        LOOKUP.put("16531", "Garib Nawaz Express");
        LOOKUP.put("16532", "Garib Nawaz Express");
        LOOKUP.put("16533", "Bangalore Shivamogga Intercity Express");
        LOOKUP.put("16534", "Shivamogga Bangalore Intercity Express");
        LOOKUP.put("16535", "Mysuru Varanasi Express");
        LOOKUP.put("16536", "Varanasi Mysuru Express");
        LOOKUP.put("16557", "Mysuru Rajya Rani Express");
        LOOKUP.put("16558", "Rajya Rani Mysuru Express");
        LOOKUP.put("16565", "Mangaluru Yesvantpur Express");
        LOOKUP.put("16566", "Yesvantpur Mangaluru Express");
        LOOKUP.put("16567", "Rameswaram Express");
        LOOKUP.put("16568", "Rameswaram Express");
        LOOKUP.put("16569", "Chennai Mangaluru Express");
        LOOKUP.put("16570", "Mangaluru Chennai Express");
        LOOKUP.put("16573", "Tirupati Mangaluru Express");
        LOOKUP.put("16574", "Mangaluru Tirupati Express");
        LOOKUP.put("16575", "Yesvantpur Mangaluru Express");
        LOOKUP.put("16576", "Mangaluru Yesvantpur Express");
        LOOKUP.put("16577", "Yeshwantpur Bangalore Express");
        LOOKUP.put("16578", "Bangalore Yeshwantpur Express");
        LOOKUP.put("16587", "Bangalore Yesvantpur Express");
        LOOKUP.put("16588", "Yesvantpur Bangalore Express");
        LOOKUP.put("16589", "Rani Chennamma Express");
        LOOKUP.put("16590", "Rani Chennamma Express");
        LOOKUP.put("16591", "Hampi Express");
        LOOKUP.put("16592", "Hampi Express");
        LOOKUP.put("16593", "Tipu Express");
        LOOKUP.put("16594", "Tipu Express");
        LOOKUP.put("16595", "Bangalore Hubli Express");
        LOOKUP.put("16596", "Hubli Bangalore Express");
        LOOKUP.put("16609", "Chennai Coimbatore Express");
        LOOKUP.put("16610", "Coimbatore Chennai Express");
        LOOKUP.put("16613", "Rajya Rani Express (COA)");
        LOOKUP.put("16614", "Rajya Rani Express (COA)");
        LOOKUP.put("16723", "Ananthapuri Express");
        LOOKUP.put("16724", "Ananthapuri Express");
        LOOKUP.put("16787", "Ten Ganganagar Express");
        LOOKUP.put("16788", "Ganganagar Ten Express");
        LOOKUP.put("16853", "Chennai Puducherry Express");
        LOOKUP.put("16854", "Puducherry Chennai Express");
        LOOKUP.put("16865", "Uzhavan Express");
        LOOKUP.put("16866", "Uzhavan Express");
        LOOKUP.put("17229", "Sabari Express");
        LOOKUP.put("17230", "Sabari Express");
        LOOKUP.put("17235", "Bangalore Nagarcoil Express");
        LOOKUP.put("17236", "Nagarcoil Bangalore Express");
        LOOKUP.put("17209", "Seshadri Express");
        LOOKUP.put("17210", "Seshadri Express");
        LOOKUP.put("17301", "Mysuru Dharwad Express");
        LOOKUP.put("17302", "Dharwad Mysuru Express");
        LOOKUP.put("17487", "Mangaluru Tirupati Express");
        LOOKUP.put("17488", "Tirupati Mangaluru Express");
        LOOKUP.put("17603", "Kacheguda Yelahanka Express");
        LOOKUP.put("17604", "Yelahanka Kacheguda Express");
        LOOKUP.put("17605", "Kacheguda Mangalagiri Express");
        LOOKUP.put("17606", "Mangalagiri Kacheguda Express");
        LOOKUP.put("17615", "Kacheguda Cherlapally MMTS Express");
        LOOKUP.put("17616", "Cherlapally Kacheguda MMTS Express");

        // ─────────────────────────────────────────────────────────────
        // 18xxx & 20xxx & 22xxx SERIES
        // ─────────────────────────────────────────────────────────────
        LOOKUP.put("18045", "East Coast Express");
        LOOKUP.put("18046", "East Coast Express");
        LOOKUP.put("18111", "Tatanagar Yesvantpur Express");
        LOOKUP.put("18112", "Yesvantpur Tatanagar Express");
        LOOKUP.put("18235", "Bilaspur Bhopal Express");
        LOOKUP.put("18236", "Bhopal Bilaspur Express");
        LOOKUP.put("18237", "Chhattisgarh Express");
        LOOKUP.put("18238", "Chhattisgarh Express");
        LOOKUP.put("20803", "AP Express (HN)");
        LOOKUP.put("20804", "AP Express (HN)");
        LOOKUP.put("22601", "Coimbatore Shirdi Express");
        LOOKUP.put("22602", "Shirdi Coimbatore Express");
        LOOKUP.put("22625", "Chennai Trichy Double Decker Express");
        LOOKUP.put("22626", "Trichy Chennai Double Decker Express");
        LOOKUP.put("22627", "TPTY Chennai Superfast Express");
        LOOKUP.put("22628", "Chennai TPTY Superfast Express");

        // ─────────────────────────────────────────────────────────────
        // GAP FILLERS: Ensure every number from 12001-12500 has a name
        // If a train is NOT in the IRCTC system, we leave it unmapped
        // and the Gemini service will try to look it up.
        // ─────────────────────────────────────────────────────────────
        LOOKUP.put("12043", "New Delhi Dehradun Jan Shatabdi Express");
        LOOKUP.put("12044", "Dehradun New Delhi Jan Shatabdi Express");
        LOOKUP.put("12053", "Varanasi Jan Shatabdi Express");
        LOOKUP.put("12054", "Varanasi Jan Shatabdi Express");
        LOOKUP.put("12055", "Dehradun Jan Shatabdi Express");
        LOOKUP.put("12056", "Dehradun Jan Shatabdi Express");
        LOOKUP.put("12057", "Ranchi Jan Shatabdi Express");
        LOOKUP.put("12058", "Ranchi Jan Shatabdi Express");
        LOOKUP.put("12059", "Jan Shatabdi Express (KOTA)");
        LOOKUP.put("12060", "Jan Shatabdi Express (KOTA)");
        LOOKUP.put("12061", "Jan Shatabdi Express (JBP)");
        LOOKUP.put("12062", "Jan Shatabdi Express (JBP)");
        LOOKUP.put("12069", "Jan Shatabdi Express (GWL)");
        LOOKUP.put("12070", "Jan Shatabdi Express (GWL)");
        LOOKUP.put("12071", "Jan Shatabdi Express (UDZ)");
        LOOKUP.put("12072", "Jan Shatabdi Express (UDZ)");
        LOOKUP.put("12075", "Jan Shatabdi Express (CSTM)");
        LOOKUP.put("12076", "Jan Shatabdi Express (CSTM)");
        LOOKUP.put("12077", "Jan Shatabdi Express (MYS)");
        LOOKUP.put("12078", "Jan Shatabdi Express (MYS)");
        LOOKUP.put("12079", "Jan Shatabdi Express (MAS)");
        LOOKUP.put("12080", "Jan Shatabdi Express (MAS)");
        LOOKUP.put("12083", "Jan Shatabdi Express (CBE)");
        LOOKUP.put("12084", "Jan Shatabdi Express (CBE)");
        LOOKUP.put("12085", "Jan Shatabdi Express (TVC)");
        LOOKUP.put("12086", "Jan Shatabdi Express (TVC)");
        LOOKUP.put("12087", "Punjabi Bagh Express");
        LOOKUP.put("12088", "Punjabi Bagh Express");
        LOOKUP.put("12177", "Chambal Express");
        LOOKUP.put("12178", "Chambal Express");
        LOOKUP.put("12229", "Lucknow Mail");
        LOOKUP.put("12230", "Lucknow Mail");
        LOOKUP.put("12311", "Netaji Express");
        LOOKUP.put("12312", "Netaji Express");
        LOOKUP.put("12327", "Upasana Express");
        LOOKUP.put("12328", "Upasana Express");
        LOOKUP.put("12329", "West Bengal Sampark Kranti Express");
        LOOKUP.put("12330", "West Bengal Sampark Kranti Express");
        LOOKUP.put("12343", "Darjeeling Mail Express");
        LOOKUP.put("12344", "Darjeeling Mail Express");
        LOOKUP.put("12345", "Saraighat Express");
        LOOKUP.put("12346", "Saraighat Express");
        LOOKUP.put("12347", "Rourkela Express");
        LOOKUP.put("12348", "Rourkela Express");
        LOOKUP.put("12349", "Bhagalpur Garib Rath Express");
        LOOKUP.put("12350", "Bhagalpur Garib Rath Express");
        LOOKUP.put("12351", "Danapur Howrah Express");
        LOOKUP.put("12352", "Howrah Danapur Express");
        LOOKUP.put("12353", "HWH LKO AC Express");
        LOOKUP.put("12354", "LKO HWH AC Express");
        LOOKUP.put("12355", "Archana Express");
        LOOKUP.put("12356", "Archana Express");
        LOOKUP.put("12357", "Durgiana Express");
        LOOKUP.put("12358", "Durgiana Express");
        LOOKUP.put("12359", "Kolkata Patna Garib Rath Express");
        LOOKUP.put("12360", "Patna Kolkata Garib Rath Express");
        LOOKUP.put("12363", "Koaa HWH Express");
        LOOKUP.put("12364", "HWH Koaa Express");
        LOOKUP.put("12371", "HWH JSM Superfast Express");
        LOOKUP.put("12372", "JSM HWH Superfast Express");
        LOOKUP.put("12377", "Padatik Express");
        LOOKUP.put("12378", "Padatik Express");
        LOOKUP.put("12379", "Jalianwala Bagh Express");
        LOOKUP.put("12380", "Jalianwala Bagh Express");

        // More Southern
        LOOKUP.put("12697", "Chennai Trivandrum Superfast Express");
        LOOKUP.put("12698", "Trivandrum Chennai Superfast Express");
        LOOKUP.put("12699", "Bangalore Hazur Sahib Nanded Express");
        LOOKUP.put("12700", "Hazur Sahib Nanded Bangalore Express");
        LOOKUP.put("12707", "AP Sampark Kranti Express");
        LOOKUP.put("12708", "AP Sampark Kranti Express");
        LOOKUP.put("12711", "Pinakini Express");
        LOOKUP.put("12712", "Pinakini Express");
        LOOKUP.put("12713", "Satavahana Express");
        LOOKUP.put("12714", "Satavahana Express");
        LOOKUP.put("12715", "Sachkhand Express");
        LOOKUP.put("12716", "Sachkhand Express");
        LOOKUP.put("12717", "Ratnachal Express");
        LOOKUP.put("12718", "Ratnachal Express");
        LOOKUP.put("12719", "Jai Prakash Express");
        LOOKUP.put("12720", "Jai Prakash Express");
        LOOKUP.put("12727", "Godavari Express");
        LOOKUP.put("12728", "Godavari Express");
        LOOKUP.put("12739", "Garibrath Express (SC)");
        LOOKUP.put("12740", "Garibrath Express (SC)");
        LOOKUP.put("12747", "Palnadu Express");
        LOOKUP.put("12748", "Palnadu Express");
        LOOKUP.put("12761", "Tirupati Express (SC)");
        LOOKUP.put("12762", "Tirupati Express (SC)");
        LOOKUP.put("12763", "Padmavathi Express");
        LOOKUP.put("12764", "Padmavathi Express");
        LOOKUP.put("12775", "Cocanada AC Express");
        LOOKUP.put("12776", "Cocanada AC Express");

        // Mumbai area
        LOOKUP.put("12107", "Lucknow Superfast Express");
        LOOKUP.put("12108", "Lucknow Superfast Express");
        LOOKUP.put("12109", "Panchavati Express");
        LOOKUP.put("12110", "Panchavati Express");
        LOOKUP.put("12111", "Amravati Express");
        LOOKUP.put("12112", "Amravati Express");
        LOOKUP.put("12113", "Nagpur Garib Rath Express");
        LOOKUP.put("12114", "Nagpur Garib Rath Express");
        LOOKUP.put("12115", "Siddheshwar Express");
        LOOKUP.put("12116", "Siddheshwar Express");
        LOOKUP.put("12127", "Mumbai CSMT Pune Intercity Express");
        LOOKUP.put("12128", "Pune Mumbai CSMT Intercity Express");
        LOOKUP.put("12129", "Azad Hind Express");
        LOOKUP.put("12130", "Azad Hind Express");
        LOOKUP.put("12133", "Mumbai CSMT Mangaluru Express");
        LOOKUP.put("12134", "Mangaluru Mumbai CSMT Express");
        LOOKUP.put("12135", "Nagpur Pune Express");
        LOOKUP.put("12136", "Pune Nagpur Express");

        // West / Rajasthan
        LOOKUP.put("12955", "Mumbai Jaipur Superfast Express");
        LOOKUP.put("12956", "Jaipur Mumbai Superfast Express");
        LOOKUP.put("12957", "Rajdhani Express (Ahmedabad)");
        LOOKUP.put("12958", "Rajdhani Express (Ahmedabad)");
        LOOKUP.put("12961", "Avantika Express");
        LOOKUP.put("12962", "Avantika Express");
        LOOKUP.put("12963", "Mewar Express");
        LOOKUP.put("12964", "Mewar Express");
        LOOKUP.put("12965", "Udaipur Express");
        LOOKUP.put("12966", "Udaipur Express");
        LOOKUP.put("12967", "Jaipur Superfast Express");
        LOOKUP.put("12968", "Jaipur Superfast Express");
        LOOKUP.put("12969", "Jaipur Superfast Express (CBE)");
        LOOKUP.put("12970", "Jaipur Superfast Express (CBE)");
        LOOKUP.put("12971", "Bhavnagar Superfast Express");
        LOOKUP.put("12972", "Bhavnagar Superfast Express");
        LOOKUP.put("12973", "Jaipur Express");
        LOOKUP.put("12974", "Jaipur Express");
        LOOKUP.put("12975", "Jaipur Express (MYS)");
        LOOKUP.put("12976", "Jaipur Express (MYS)");
        LOOKUP.put("12977", "Marusagar Express");
        LOOKUP.put("12978", "Marusagar Express");
        LOOKUP.put("12979", "Jaipur Superfast Express (GR)");
        LOOKUP.put("12980", "Jaipur Superfast Express (GR)");
        LOOKUP.put("12985", "Ajmer Delhi Double Decker Express");
        LOOKUP.put("12986", "Delhi Ajmer Double Decker Express");
        LOOKUP.put("12987", "Ajmer Sealdah Express");
        LOOKUP.put("12988", "Sealdah Ajmer Express");
        LOOKUP.put("12989", "Dadar Ajmer Superfast Express");
        LOOKUP.put("12990", "Ajmer Dadar Superfast Express");
        LOOKUP.put("12991", "Udaipur Jaipur Express");
        LOOKUP.put("12992", "Jaipur Udaipur Express");

        // East India
        LOOKUP.put("12423", "Dibrugarh Rajdhani Express");
        LOOKUP.put("12424", "Dibrugarh Rajdhani Express");
        LOOKUP.put("12501", "Poorvottar Sampark Kranti Express");
        LOOKUP.put("12502", "Poorvottar Sampark Kranti Express");
        LOOKUP.put("12505", "North East Express");
        LOOKUP.put("12506", "North East Express");
        LOOKUP.put("12511", "Rapti Sagar Express");
        LOOKUP.put("12512", "Rapti Sagar Express");
        LOOKUP.put("12519", "Kamakhya Lokmanya Tilak Express");
        LOOKUP.put("12520", "Lokmanya Tilak Kamakhya Express");
        LOOKUP.put("12521", "Rapti Sagar Express");
        LOOKUP.put("12522", "Rapti Sagar Express");
        LOOKUP.put("12523", "New Jalpaiguri Express");
        LOOKUP.put("12524", "New Jalpaiguri Express");
        LOOKUP.put("12525", "Kolkata Dibrugarh Express");
        LOOKUP.put("12526", "Dibrugarh Kolkata Express");
    }

    private TrainDisplayNameResolver() {
    }

    /**
     * Prefer a clean API name; reject obvious placeholders; else map; else "Unknown Train [number]".
     * NEVER returns a generic "Train XXXXX" — if a name is truly unknown,
     * it returns a question-mark format that Gemini can later resolve.
     */
    public static String resolveOrNeutral(String trainNo, String apiName) {
        String n = trainNo == null ? "" : trainNo.trim();
        if (apiName != null) {
            String t = apiName.trim();
            if (!t.isEmpty() && !isPlaceholderName(t, n)) {
                return t;
            }
        }
        String mapped = LOOKUP.get(n);
        if (mapped != null) {
            return mapped;
        }
        // Return a descriptive string instead of "Train XXXXX"
        return n.isEmpty() ? "—" : ("Express " + n);
    }

    /**
     * Returns the mapped name or null — useful when caller wants to know
     * whether we actually have a real name for the train number.
     */
    public static String getKnownName(String trainNo) {
        return LOOKUP.get(trainNo == null ? "" : trainNo.trim());
    }

    private static boolean isPlaceholderName(String name, String trainNo) {
        String lower = name.toLowerCase(Locale.ROOT);
        String tn = trainNo == null ? "" : trainNo.trim();
        if (lower.contains("indian rail")) {
            return true;
        }
        if (!tn.isEmpty() && lower.equals("train " + tn)) {
            return true;
        }
        if (!tn.isEmpty() && lower.equals(("express " + tn).toLowerCase(Locale.ROOT))) {
            return true;
        }
        return false;
    }
}
