package com.slowly.manmanlai.ui

import androidx.compose.ui.graphics.Color
import com.slowly.manmanlai.R
import com.slowly.manmanlai.TemplateColors
import com.slowly.manmanlai.ThemeTemplate

data class CardStyle(
    val id: String,
    val name: String,
    val front: Long,
    val back: Long,
    val border: Long,
    val accent: Long,
    val text: Long,
    val imageRes: Int,
)

val cardStyles = listOf(
    CardStyle("fresh_ai", "\u6e05\u723d", 0xFFFFFEFA, 0xFFFFF7EF, 0xFFE76F51, 0xFFE76F51, 0xFF2B211D, R.drawable.card_style_fresh),
    CardStyle("dark_ai", "\u591c\u884c", 0xFF252B28, 0xFF303934, 0xFF79D2A6, 0xFF79D2A6, 0xFFF1F6F2, R.drawable.card_style_dark),
    CardStyle("eye_ai", "\u62a4\u773c", 0xFFFCFFFD, 0xFFEAF7EF, 0xFF6E9F4E, 0xFF6E9F4E, 0xFF26351E, R.drawable.card_style_eye),
    CardStyle("coral_ai", "\u6d3b\u529b", 0xFFFFFBF8, 0xFFFFE5D9, 0xFFE86F51, 0xFFE86F51, 0xFF3C2720, R.drawable.card_style_coral),
)

val themeTemplates = listOf(
    ThemeTemplate(
        id = "fresh",
        name = "\u96e8\u540e\u6e05\u6668",
        colorScheme = TemplateColors(0xFFEAF8F1, 0xFFFFFFFF, 0xFFD6F0E4, 0xFF2F9C75, 0xFF17372C),
    ),
    ThemeTemplate(
        id = "dark",
        name = "\u6df1\u591c\u5de5\u4f5c\u53f0",
        colorScheme = TemplateColors(0xFF101715, 0xFF202A27, 0xFF17221F, 0xFF7DDEB0, 0xFFF2FBF5),
    ),
    ThemeTemplate(
        id = "eye",
        name = "\u7af9\u7eb8\u62a4\u773c",
        colorScheme = TemplateColors(0xFFF1F8E8, 0xFFFFFFF7, 0xFFDCECCB, 0xFF68994A, 0xFF26381E),
    ),
    ThemeTemplate(
        id = "coral",
        name = "\u65e5\u843d\u884c\u52a8",
        colorScheme = TemplateColors(0xFFFFF0EA, 0xFFFFFCF8, 0xFFFFD8C7, 0xFFE85D45, 0xFF402119),
    ),
    ThemeTemplate(
        id = "library",
        name = "\u6728\u684c\u4e66\u623f",
        colorScheme = TemplateColors(0xFFF3E9D9, 0xFFFFFBF2, 0xFFE2CEAD, 0xFF8A633B, 0xFF2F2114),
    ),
    ThemeTemplate(
        id = "ocean",
        name = "\u6d77\u76d0\u84dd\u98ce",
        colorScheme = TemplateColors(0xFFE8F8FB, 0xFFFFFFFF, 0xFFCBEAF1, 0xFF247F9E, 0xFF143642),
    ),
    ThemeTemplate(
        id = "sakura",
        name = "\u8584\u6a31\u590d\u76d8",
        colorScheme = TemplateColors(0xFFFFF0F5, 0xFFFFFCFD, 0xFFF7D5E2, 0xFFD95C8A, 0xFF422333),
    ),
    ThemeTemplate(
        id = "studio",
        name = "\u94c5\u7b14\u5de5\u4f5c\u53f0",
        colorScheme = TemplateColors(0xFFF1F4F7, 0xFFFFFFFF, 0xFFDDE6EE, 0xFF496F93, 0xFF202A34),
    ),
)

fun templateOf(id: String): ThemeTemplate = themeTemplates.firstOrNull { it.id == id } ?: themeTemplates.first()
fun cardStyleOf(id: String): CardStyle = cardStyles.firstOrNull { it.id == id } ?: cardStyles.first()
fun Long.asColor(): Color = Color(this)
