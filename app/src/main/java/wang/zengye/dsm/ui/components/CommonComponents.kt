package wang.zengye.dsm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import wang.zengye.dsm.ui.theme.*

// ==================== 状态徽章组件 ====================

/**
 * 状态徽章
 */
@Composable
fun StatusBadge(
    text: String,
    status: BadgeStatus = BadgeStatus.DEFAULT,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, contentColor) = when (status) {
        BadgeStatus.SUCCESS -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        BadgeStatus.WARNING -> MaterialTheme.warningContainer to MaterialTheme.onWarningContainer
        BadgeStatus.ERROR -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        BadgeStatus.INFO -> MaterialTheme.infoContainer to MaterialTheme.onInfoContainer
        BadgeStatus.DEFAULT -> MaterialTheme.colorScheme.surfaceContainerHigh to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(CornerRadius.Full),
        color = backgroundColor,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

enum class BadgeStatus {
    DEFAULT, SUCCESS, WARNING, ERROR, INFO
}

// ==================== 分组标题组件 ====================

/**
 * 分组标题
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = Spacing.PageHorizontal,
                end = Spacing.PageHorizontal,
                top = Spacing.Small,
                bottom = Spacing.Small
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧主色竖条装饰
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(CornerRadius.Full))
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(Spacing.MediumSmall))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        if (action != null) {
            action()
        }
    }
}
