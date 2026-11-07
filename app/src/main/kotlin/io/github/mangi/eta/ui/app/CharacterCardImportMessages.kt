package io.github.mangi.eta.ui.app

/** 导入只公开受控分类，文件内容与底层异常文本不进入界面。 */
internal fun characterCardImportMessage(code: String?): String? = when (code) {
    "CARD_V3_INVALID" -> "Të dhënat V3 të personazhit në PNG janë dëmtuar; nuk u përdor V2. Shkarko sërish kartën origjinale."
    "CARD_METADATA_MISSING" -> "Kjo PNG nuk përmban të dhëna personazhi. Zgjidh kartën origjinale, jo pamje ekrani ose imazh të kompresuar."
    "CARD_INVALID_JSON" -> "JSON-i i kartës së personazhit është i pavlefshëm. Kontrollo skedarin."
    "CARD_TOO_LARGE" -> "Karta tejkalon kufirin e madhësisë. Thjeshto përmbajtjen ose zgjidh skedar më të vogël."
    "CARD_INVALID_UTF8" -> "Kodimi i tekstit është i pavlefshëm. Kërkohet JSON me UTF-8."
    "CARD_UNSUPPORTED_SPEC" -> "Formati i kësaj karte nuk mbështetet. Eksportoje si V2 ose V3 dhe provo përsëri."
    "CARD_INVALID_PNG" -> "PNG-ja është dëmtuar ose e paplotë. Shkarko përsëri kartën origjinale."
    "CARD_INVALID_DATA" -> "Fushat e kartës janë të pavlefshme. Kontrollo emrin, tekstin hyrës dhe të dhënat e historisë."
    else -> null
}
