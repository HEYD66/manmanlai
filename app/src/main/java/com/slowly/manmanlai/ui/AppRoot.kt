package com.slowly.manmanlai.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slowly.manmanlai.Achievement
import com.slowly.manmanlai.CompletedCard
import com.slowly.manmanlai.DeckLogic
import com.slowly.manmanlai.PlanTask
import com.slowly.manmanlai.Priority
import com.slowly.manmanlai.R
import com.slowly.manmanlai.formatDateTime
import kotlin.math.roundToInt
import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.sin
import kotlinx.coroutines.launch

private enum class Tab(val label: String) {
    Deck("\u724c\u5806"),
    Pack("\u5361\u5305"),
    Summary("\u603b\u7ed3"),
    Settings("\u8bbe\u7f6e"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManManLaiAppRoot(viewModel: ManManLaiViewModel) {
    val state by viewModel.uiState.collectAsState()
    val template = templateOf(state.themeId)
    val colors = template.colorScheme
    var tab by remember { mutableStateOf(Tab.Deck) }
    var showAdd by remember { mutableStateOf(false) }
    var backupText by remember { mutableStateOf("") }
    var showBackup by remember { mutableStateOf(false) }

    LaunchedEffect(state.focusTaskId) {
        if (state.focusTaskId != null) {
            tab = Tab.Deck
            viewModel.consumeFocusTask()
        }
    }

    MaterialTheme(
        colorScheme = appColorScheme(template),
        typography = appTypography(template.id),
    ) {
        Surface(Modifier.fillMaxSize(), color = Color.Transparent) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = colors.cardFront.asColor(),
                            titleContentColor = colors.text.asColor(),
                            actionIconContentColor = colors.accent.asColor(),
                        ),
                        title = {
                            Column {
                                Text("\u6162\u6162\u6765", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                                Text("\u50cf\u7ffb\u724c\u4e00\u6837\u5904\u7406\u8ba1\u5212", style = MaterialTheme.typography.labelMedium)
                            }
                        },
                        actions = {
                            if (tab == Tab.Deck) {
                                TextButton(onClick = { showAdd = true }) { Text("\u65b0\u589e") }
                            }
                        },
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = colors.cardBack.asColor(),
                        tonalElevation = 0.dp,
                    ) {
                        Tab.entries.forEach {
                            NavigationBarItem(
                                selected = tab == it,
                                onClick = { tab = it },
                                icon = { Text(iconFor(it)) },
                                label = { Text(it.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = colors.text.asColor(),
                                    selectedTextColor = colors.text.asColor(),
                                    indicatorColor = colors.accent.asColor().copy(alpha = 0.22f),
                                    unselectedIconColor = colors.text.asColor().copy(alpha = 0.72f),
                                    unselectedTextColor = colors.text.asColor().copy(alpha = 0.72f),
                                ),
                            )
                        }
                    }
                },
            ) { padding ->
                Box(
                    Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .background(themeBackground(template.id)),
                ) {
                    ThemeImageLayer(template.id, Modifier.matchParentSize())
                    if (!hasThemeImage(template.id)) {
                        ThemeAtmosphere(template.id, Modifier.matchParentSize())
                    }
                    when (tab) {
                        Tab.Deck -> DeckScreen(
                            tasks = state.deck,
                            cardStyleId = state.cardStyleId,
                            onCycle = viewModel::cycleTask,
                            onComplete = viewModel::complete,
                            onPostpone = viewModel::postpone,
                            onDelete = viewModel::delete,
                            onEdit = viewModel::editTask,
                        )
                        Tab.Pack -> CardPackScreen(
                            cards = state.cards,
                            onDelete = viewModel::deleteCompleted,
                            onReturn = viewModel::returnCardToDeck,
                        )
                        Tab.Summary -> SummaryScreen(state.deck, state.cards, state.achievements)
                        Tab.Settings -> SettingsScreen(
                            selectedTheme = state.themeId,
                            selectedCardStyle = state.cardStyleId,
                            trash = state.trash,
                            onTheme = viewModel::changeTheme,
                            onCardStyle = viewModel::changeCardStyle,
                            onRestore = viewModel::restore,
                            onPurge = viewModel::purge,
                            onExport = {
                                viewModel.exportBackup {
                                    backupText = it
                                    showBackup = true
                                }
                            },
                            onImport = {
                                backupText = ""
                                showBackup = true
                            },
                        )
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddTaskDialog(
            onDismiss = { showAdd = false },
            onAdd = { title, desc, tags, minutes, priority, dueAt ->
                viewModel.addTask(title, desc, tags, minutes, priority, dueAt)
                showAdd = false
            },
        )
    }
    if (showBackup) {
        BackupDialog(
            initialText = backupText,
            onDismiss = { showBackup = false },
            onImport = {
                viewModel.importBackup(it)
                showBackup = false
            },
        )
    }
}

@Composable
private fun DeckScreen(
    tasks: List<PlanTask>,
    cardStyleId: String,
    onCycle: (PlanTask) -> Unit,
    onComplete: (PlanTask) -> Unit,
    onPostpone: (PlanTask) -> Unit,
    onDelete: (PlanTask) -> Unit,
    onEdit: (PlanTask, String, String, String, Int?, Priority, Long?) -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
        if (tasks.isEmpty()) {
            EmptyDeck()
        } else {
            val widthFromHeight = maxHeight * (5f / 7f)
            val cardWidth = minOf(maxWidth * 0.86f, widthFromHeight * 0.96f, 365.dp)
            val visibleTasks = tasks.take(5)
            visibleTasks.reversed().forEach { task ->
                val fanSlot = visibleTasks.indexOf(task)
                TaskPlayingCard(
                    task = task,
                    cardStyleId = cardStyleId,
                    isTop = task == tasks.first(),
                    stackIndex = fanSlot,
                    cardWidth = cardWidth,
                    onCycle = onCycle,
                    onComplete = onComplete,
                    onPostpone = onPostpone,
                    onDelete = onDelete,
                    onEdit = onEdit,
                )
            }
        }
    }
}

@Composable
private fun TaskPlayingCard(
    task: PlanTask,
    cardStyleId: String,
    isTop: Boolean,
    stackIndex: Int,
    cardWidth: Dp,
    onCycle: (PlanTask) -> Unit,
    onComplete: (PlanTask) -> Unit,
    onPostpone: (PlanTask) -> Unit,
    onDelete: (PlanTask) -> Unit,
    onEdit: (PlanTask, String, String, String, Int?, Priority, Long?) -> Unit,
) {
    val style = cardStyleOf(cardStyleId)
    val cardClickSource = remember { MutableInteractionSource() }
    var flipped by remember(task.id) { mutableStateOf(false) }
    var offset by remember(task.id) { mutableStateOf(Offset.Zero) }
    var editing by remember(task.id) { mutableStateOf(false) }
    var settling by remember(task.id) { mutableStateOf(false) }
    var departed by remember(task.id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
        label = "flip",
    )
    val flipLift = sin(Math.toRadians(rotation.toDouble())).toFloat().coerceAtLeast(0f)
    val showingBack = rotation > 90f
    val flipScaleX = kotlin.math.abs(kotlin.math.cos(Math.toRadians(rotation.toDouble()))).toFloat().coerceAtLeast(0.08f)
    val shape = RoundedCornerShape(18.dp)
    LaunchedEffect(isTop) {
        if (!isTop) {
            offset = Offset.Zero
            settling = false
            departed = false
        }
    }
    val dragModifier = if (isTop && !settling) {
        Modifier.pointerInput(task.id) {
            detectDragGestures(
                onDrag = { change, dragAmount ->
                    change.consume()
                    offset += dragAmount
                },
                onDragEnd = {
                    val shouldCycle = DeckLogic.shouldCycleCard(offset.x, offset.y, size.width.toFloat(), size.height.toFloat())
                    val start = offset
                    settling = true
                    scope.launch {
                        val progress = Animatable(0f)
                        if (shouldCycle) {
                            val target = if (abs(start.x) >= abs(start.y)) {
                                Offset(sign(start.x).takeIf { it != 0f } ?: 1f, start.y / size.height)
                                    .let { Offset(it.x * size.width * 1.45f, it.y * size.height + start.y) }
                            } else {
                                Offset(start.x, (sign(start.y).takeIf { it != 0f } ?: 1f) * size.height * 1.3f)
                            }
                            progress.animateTo(1f, tween(210, easing = FastOutSlowInEasing)) {
                                offset = Offset(
                                    x = start.x + (target.x - start.x) * value,
                                    y = start.y + (target.y - start.y) * value,
                                )
                            }
                            departed = true
                            onCycle(task)
                        } else {
                            progress.animateTo(1f, tween(220, easing = FastOutSlowInEasing)) {
                                val easedBack = 1f - value
                                offset = Offset(start.x * easedBack, start.y * easedBack)
                            }
                            offset = Offset.Zero
                            settling = false
                        }
                    }
                },
                onDragCancel = { offset = Offset.Zero },
            )
        }
    } else {
        Modifier
    }
    val fanX = when (stackIndex) {
        1 -> (-28).dp
        2 -> 28.dp
        3 -> (-52).dp
        4 -> 52.dp
        else -> 0.dp
    }
    val fanY = when (stackIndex) {
        1, 2 -> 18.dp
        3, 4 -> 34.dp
        else -> 0.dp
    }
    val fanRotation = when (stackIndex) {
        1 -> -8f
        2 -> 8f
        3 -> -14f
        4 -> 14f
        else -> 0f
    }
    val fanScale = when (stackIndex) {
        0 -> 1f
        1, 2 -> 0.97f
        else -> 0.94f
    }

    Box(
        modifier = Modifier
            .width(cardWidth)
            .aspectRatio(5f / 7f)
            .offset(x = fanX, y = fanY)
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .graphicsLayer {
                scaleX = fanScale
                scaleY = fanScale
                rotationZ = if (isTop) offset.x / 36f else fanRotation
                alpha = if (departed) 0f else 1f
            }
            .then(dragModifier)
            .clickable(
                enabled = isTop,
                interactionSource = cardClickSource,
                indication = null,
            ) { flipped = !flipped },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX *= if (isTop) flipScaleX else 1f
                    translationY = -10f * density * flipLift
                    scaleY = 1f + 0.01f * flipLift
                    this.shape = shape
                    clip = true
                }
                .clip(shape)
                .background(cardBrush(cardStyleId, showingBack), shape)
        ) {
            Image(
                painter = painterResource(style.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(shape),
            )
            Box(Modifier.fillMaxSize().border(2.dp, style.border.asColor(), shape))
            if (!isTop) {
                Box(Modifier.fillMaxSize().padding(18.dp)) {
                    CardFront(
                        task = task,
                        cardStyleId = cardStyleId,
                        onComplete = {},
                        onPostpone = {},
                        onDelete = {},
                        onEdit = {},
                        showControls = false,
                    )
                }
            } else if (!showingBack) {
                Box(Modifier.fillMaxSize().padding(18.dp)) {
                    CardFront(task, cardStyleId, onComplete, onPostpone, onDelete, onEdit = { editing = true })
                }
            } else {
                Box(Modifier.fillMaxSize().padding(18.dp)) {
                    CardBack(task, cardStyleId)
                }
            }
        }
    }
    if (editing) {
        TaskEditDialog(
            titleText = "\u7f16\u8f91\u8ba1\u5212\u5361",
            task = task,
            onDismiss = { editing = false },
            onSave = { title, desc, tags, minutes, priority, dueAt ->
                onEdit(task, title, desc, tags, minutes, priority, dueAt)
                editing = false
            },
        )
    }
}

@Composable
private fun CardFront(
    task: PlanTask,
    cardStyleId: String,
    onComplete: (PlanTask) -> Unit,
    onPostpone: (PlanTask) -> Unit,
    onDelete: (PlanTask) -> Unit,
    onEdit: () -> Unit,
    showControls: Boolean = true,
) {
    val style = cardStyleOf(cardStyleId)
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
        Column {
            PokerCorner(task.id.takeCardCode(), style.accent.asColor())
            Spacer(Modifier.height(12.dp))
            Text(
                task.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = style.text.asColor(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text("#${task.id}", style = MaterialTheme.typography.labelMedium, color = style.text.asColor().copy(alpha = 0.68f))
            Spacer(Modifier.height(10.dp))
            Text(
                task.description.ifBlank { "\u5148\u505a\u4e00\u70b9\u70b9\uff0c\u4e5f\u7b97\u6570\u3002" },
                style = MaterialTheme.typography.bodyLarge,
                color = style.text.asColor().copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("\u4f18\u5148\u7ea7 ${task.priority.label()}", color = style.text.asColor().copy(alpha = 0.72f), style = MaterialTheme.typography.labelMedium)
                task.dueAt?.let {
                    Text(
                        "\u622a\u6b62 ${it.formatDateTime()}",
                        color = style.text.asColor().copy(alpha = 0.72f),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            task.reminderCycleMinutes?.let {
                Text("\u6bcf $it \u5206\u949f\u6e29\u548c\u63d0\u9192", color = style.text.asColor().copy(alpha = 0.72f), style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val chipTextColor = if (cardStyleId == "dark_ai") style.text.asColor() else style.accent.asColor()
                val chipContainerColor = if (cardStyleId == "dark_ai") Color.White.copy(alpha = 0.08f) else style.accent.asColor().copy(alpha = 0.08f)
                task.tags.take(2).forEach {
                    AssistChip(
                        onClick = {},
                        label = { Text(it, color = chipTextColor, fontWeight = FontWeight.Bold) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = chipContainerColor,
                            labelColor = chipTextColor,
                        ),
                        border = BorderStroke(1.dp, chipTextColor.copy(alpha = 0.6f)),
                    )
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "\u521b\u5efa ${task.createdAt.formatDateTime()}",
                color = style.text.asColor().copy(alpha = 0.65f),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (showControls) {
                Button(
                    onClick = { onComplete(task) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("\u5b8c\u6210\uff0c\u6536\u5165\u5361\u5305") }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val actionPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    OutlinedButton(
                        onClick = { onPostpone(task) },
                        modifier = Modifier.weight(1f),
                        contentPadding = actionPadding,
                    ) { Text("\u7a0d\u540e", fontSize = 14.sp, maxLines = 1) }
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        contentPadding = actionPadding,
                    ) { Text("\u7f16\u8f91", fontSize = 14.sp, maxLines = 1) }
                    OutlinedButton(
                        onClick = { onDelete(task) },
                        modifier = Modifier.weight(1f),
                        contentPadding = actionPadding,
                    ) { Text("\u5220\u9664", fontSize = 14.sp, maxLines = 1) }
                }
            } else {
                Spacer(Modifier.height(104.dp))
            }
        }
    }
}

@Composable
private fun CardBack(task: PlanTask, cardStyleId: String) {
    val style = cardStyleOf(cardStyleId)
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
        Text("\u6162\u6162\u6765", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = style.accent.asColor())
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(task.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = style.text.asColor())
            Text("\u521b\u5efa\u65f6\u95f4\n${task.createdAt.formatDateTime()}", color = style.text.asColor())
            Text("\u5b8c\u6210\u65f6\u95f4\n${task.completedAt?.formatDateTime() ?: "\u8fd8\u5728\u8def\u4e0a"}", color = style.text.asColor())
            Text("\u5ef6\u671f\u6b21\u6570 ${task.postponeCount}", color = style.text.asColor())
            Text("\u72b6\u6001 ${task.status.label()}", color = style.text.asColor())
        }
        PokerCorner("M", style.accent.asColor())
    }
}

@Composable
private fun PokerCorner(text: String, color: Color) {
    Box(
        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text.uppercase(), style = MaterialTheme.typography.labelLarge, color = color, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun CardPackScreen(
    cards: List<CompletedCard>,
    onDelete: (CompletedCard) -> Unit,
    onReturn: (CompletedCard) -> Unit,
) {
    var selected by remember { mutableStateOf<CompletedCard?>(null) }
    if (cards.isEmpty()) {
        EmptyDeck("\u5b8c\u6210\u7684\u5361\u7247\u4f1a\u5b58\u8fdb\u8fd9\u91cc\u3002")
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(cards, key = { it.id }) { card -> CompletedPlayingCard(card, onClick = { selected = card }) }
    }
    selected?.let { card ->
        AlertDialog(
            onDismissRequest = { selected = null },
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(card.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Text("\u5b8c\u6210\u5361 #${card.id}", style = MaterialTheme.typography.labelMedium)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DetailBlock("\u5b8c\u6210\u603b\u7ed3", card.summary.ifBlank { "\u8fd9\u5f20\u5361\u5df2\u5b8c\u6210\uff0c\u7ed9\u81ea\u5df1\u8bb0\u4e00\u7b14\u3002" })
                    DetailRow("\u4efb\u52a1\u540d\u79f0", card.title)
                    DetailRow("\u4efb\u52a1\u7ec6\u8282", card.description.ifBlank { "\u672a\u586b\u5199" })
                    DetailRow("\u6807\u7b7e", card.tags.ifEmpty { listOf("\u65e0") }.joinToString("  "))
                    DetailRow("\u4f18\u5148\u7ea7", card.priority.label())
                    DetailRow("\u6765\u6e90\u4efb\u52a1", "#${card.sourceTaskId}")
                    DetailRow("\u521b\u5efa\u65f6\u95f4", card.createdAt.formatDateTime())
                    DetailRow("\u5b8c\u6210\u65f6\u95f4", card.completedAt.formatDateTime())
                    card.dueAt?.let { DetailRow("\u539f\u622a\u6b62\u65f6\u95f4", it.formatDateTime()) }
                    card.reminderCycleMinutes?.let { DetailRow("\u539f\u63d0\u9192\u5468\u671f", "\u6bcf $it \u5206\u949f") }
                    DetailRow("\u5ef6\u671f\u8bb0\u5f55", "${card.postponeCount} \u6b21${card.delayReason.takeIf { it.isNotBlank() }?.let { "\uff0c$it" }.orEmpty()}")
                    DetailRow("\u4f7f\u7528\u724c\u9762", cardStyleOf(card.templateId).name)
                    DetailRow(
                        "\u6210\u5c31\u8bb0\u5f55",
                        if (card.achievementIds.isEmpty()) "\u6682\u65e0\u65b0\u6210\u5c31" else card.achievementIds.joinToString(prefix = "#"),
                    )
                }
            },
            confirmButton = { Button(onClick = { onReturn(card); selected = null }) { Text("\u8fd4\u56de\u724c\u5806") } },
            dismissButton = { TextButton(onClick = { onDelete(card); selected = null }) { Text("\u79fb\u5230\u56de\u6536\u7ad9") } },
        )
    }
}

@Composable
private fun DetailBlock(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CompletedPlayingCard(card: CompletedCard, onClick: () -> Unit) {
    val cardStyleId = card.templateId
    val style = cardStyleOf(cardStyleId)
    Box(
        modifier = Modifier
            .aspectRatio(5f / 7f)
            .clip(RoundedCornerShape(14.dp))
            .background(cardBrush(cardStyleId, false))
            .border(1.dp, style.border.asColor(), RoundedCornerShape(14.dp))
            .clickable { onClick() },
    ) {
        Image(
            painter = painterResource(style.imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            PokerCorner(card.title.take(1), style.accent.asColor())
            Column {
                Text(card.title, fontWeight = FontWeight.Bold, color = style.text.asColor(), maxLines = 2)
                Spacer(Modifier.height(8.dp))
                Text(card.summary, color = style.text.asColor().copy(alpha = 0.75f), maxLines = 3)
            }
            Text(card.completedAt.formatDateTime(), color = style.text.asColor().copy(alpha = 0.65f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun TrashScreen(tasks: List<PlanTask>, onRestore: (PlanTask) -> Unit, onPurge: (PlanTask) -> Unit) {
    if (tasks.isEmpty()) {
        EmptyDeck("\u5220\u9664\u7684\u724c\u4f1a\u5148\u56de\u5230\u8fd9\u91cc\u3002")
        return
    }
    LazyColumn(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(tasks) { task ->
            Card {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(task.title, fontWeight = FontWeight.Bold)
                    Text(task.description, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { onRestore(task) }, modifier = Modifier.weight(1f)) { Text("\u590d\u539f") }
                        OutlinedButton(onClick = { onPurge(task) }, modifier = Modifier.weight(1f)) { Text("\u5f7b\u5e95\u5220\u9664") }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryScreen(tasks: List<PlanTask>, cards: List<CompletedCard>, achievements: List<Achievement>) {
    val zone = java.time.ZoneId.systemDefault()
    val today = java.time.LocalDate.now()
    val todayStart = today.atStartOfDay(zone).toInstant().toEpochMilli()
    val weekStart = today.minusDays((today.dayOfWeek.value - 1).toLong())
        .atStartOfDay(zone).toInstant().toEpochMilli()
    LazyColumn(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("\u4eca\u65e5", cards.count { it.completedAt >= todayStart }.toString(), Modifier.weight(1f))
                StatCard("\u672c\u5468", cards.count { it.completedAt >= weekStart }.toString(), Modifier.weight(1f))
                StatCard("\u8fde\u7eed", "${currentStreak(cards)} \u5929", Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("\u5f85\u529e", tasks.size.toString(), Modifier.weight(1f))
                StatCard("\u5361\u5305", cards.size.toString(), Modifier.weight(1f))
                StatCard("\u6210\u5c31", achievements.size.toString(), Modifier.weight(1f))
            }
        }
        item { Text("\u6210\u5c31", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        if (achievements.isEmpty()) {
            item { Text("\u5b8c\u6210\u7b2c\u4e00\u5f20\u5361\uff0c\u6210\u5c31\u4f1a\u4ece\u8fd9\u91cc\u5f00\u59cb\u3002") }
        }
        items(achievements) {
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text(it.title, fontWeight = FontWeight.Bold)
                    Text(it.description)
                }
            }
        }
    }
}

private fun currentStreak(cards: List<CompletedCard>): Int {
    val zone = java.time.ZoneId.systemDefault()
    val completedDates = cards
        .map { java.time.Instant.ofEpochMilli(it.completedAt).atZone(zone).toLocalDate() }
        .toSet()
    var cursor = java.time.LocalDate.now()
    if (cursor !in completedDates) cursor = cursor.minusDays(1)
    var streak = 0
    while (cursor in completedDates) {
        streak++
        cursor = cursor.minusDays(1)
    }
    return streak
}

@Composable
private fun SettingsScreen(
    selectedTheme: String,
    selectedCardStyle: String,
    trash: List<PlanTask>,
    onTheme: (String) -> Unit,
    onCardStyle: (String) -> Unit,
    onRestore: (PlanTask) -> Unit,
    onPurge: (PlanTask) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
) {
    var cardExpanded by remember { mutableStateOf(false) }
    var themeExpanded by remember { mutableStateOf(false) }
    var trashExpanded by remember { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Button(onClick = { trashExpanded = !trashExpanded }, modifier = Modifier.fillMaxWidth()) {
                Text(if (trashExpanded) "\u6536\u8d77\u56de\u6536\u7ad9" else "\u5c55\u5f00\u56de\u6536\u7ad9\uff08${trash.size}\uff09")
            }
        }
        if (trashExpanded) {
            if (trash.isEmpty()) {
                item {
                    Card {
                        Text(
                            "\u56de\u6536\u7ad9\u662f\u7a7a\u7684\u3002",
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            } else {
                items(trash) { task ->
                    Card {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(task.title, fontWeight = FontWeight.Bold)
                            Text(task.description, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(onClick = { onRestore(task) }, modifier = Modifier.weight(1f)) { Text("\u590d\u539f") }
                                OutlinedButton(onClick = { onPurge(task) }, modifier = Modifier.weight(1f)) { Text("\u5f7b\u5e95\u5220\u9664") }
                            }
                        }
                    }
                }
            }
        }
        item {
            Button(onClick = { cardExpanded = !cardExpanded }, modifier = Modifier.fillMaxWidth()) {
                Text(if (cardExpanded) "\u6536\u8d77\u724c\u9762\u6837\u5f0f" else "\u5c55\u5f00\u724c\u9762\u6837\u5f0f")
            }
        }
        if (cardExpanded) items(cardStyles) { style ->
            val selected = selectedCardStyle == style.id
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onCardStyle(style.id) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .height(54.dp)
                            .width(38.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(cardBrush(style.id, false))
                            .border(1.dp, style.border.asColor(), RoundedCornerShape(5.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(style.imageRes),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(5.dp)),
                        )
                        Text("M", color = style.accent.asColor(), fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(style.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    Text(if (selected) "\u4f7f\u7528\u4e2d" else "\u5207\u6362", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        item {
            Button(onClick = { themeExpanded = !themeExpanded }, modifier = Modifier.fillMaxWidth()) {
                Text(if (themeExpanded) "\u6536\u8d77\u4e3b\u9898" else "\u5c55\u5f00\u4e3b\u9898")
            }
        }
        if (themeExpanded) items(themeTemplates) { template ->
            val selected = selectedTheme == template.id
            Card(
                colors = CardDefaults.cardColors(containerColor = template.colorScheme.cardFront.asColor()),
                modifier = Modifier.fillMaxWidth().clickable { onTheme(template.id) },
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    ThemePreviewSwatch(template.id, template.colorScheme.accent.asColor())
                    Spacer(Modifier.width(12.dp))
                    Text(template.name, fontWeight = FontWeight.Bold, color = template.colorScheme.text.asColor(), modifier = Modifier.weight(1f))
                    Text(if (selected) "\u4f7f\u7528\u4e2d" else "\u5207\u6362", color = template.colorScheme.accent.asColor())
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onExport, modifier = Modifier.weight(1f)) { Text("\u5bfc\u51fa\u5907\u4efd") }
                OutlinedButton(onClick = onImport, modifier = Modifier.weight(1f)) { Text("\u5bfc\u5165\u5907\u4efd") }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label)
        }
    }
}

@Composable
private fun EmptyDeck(text: String = "\u724c\u5806\u7a7a\u4e86\uff0c\u65b0\u589e\u4e00\u5f20\u8ba1\u5212\u5361\u5427\u3002") {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("\u6162\u6162\u6765", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(text)
    }
}

@Composable
private fun AddTaskDialog(onDismiss: () -> Unit, onAdd: (String, String, String, Int?, Priority, Long?) -> Unit) {
    TaskEditDialog(
        titleText = "\u65b0\u589e\u8ba1\u5212\u5361",
        task = null,
        onDismiss = onDismiss,
        onSave = onAdd,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskEditDialog(
    titleText: String,
    task: PlanTask?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int?, Priority, Long?) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var reminderMinutes by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.NORMAL) }
    var dueAt by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingDate by remember { mutableStateOf<java.time.LocalDate?>(null) }
    androidx.compose.runtime.LaunchedEffect(task?.id) {
        title = task?.title.orEmpty()
        desc = task?.description.orEmpty()
        tags = task?.tags?.joinToString(" ").orEmpty()
        reminderMinutes = task?.reminderCycleMinutes?.toString().orEmpty()
        priority = task?.priority ?: Priority.NORMAL
        dueAt = task?.dueAt
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titleText) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(title, { title = it }, label = { Text("\u4efb\u52a1\u540d\u79f0") }, singleLine = true)
                OutlinedTextField(desc, { desc = it }, label = { Text("\u4efb\u52a1\u7ec6\u8282") }, minLines = 3)
                OutlinedTextField(tags, { tags = it }, label = { Text("\u6807\u7b7e\uff0c\u7528\u7a7a\u683c\u5206\u9694") }, singleLine = true)
                Text("\u4f18\u5148\u7ea7", style = MaterialTheme.typography.labelMedium)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    Priority.entries.forEachIndexed { index, item ->
                        SegmentedButton(
                            selected = priority == item,
                            onClick = { priority = item },
                            shape = SegmentedButtonDefaults.itemShape(index, Priority.entries.size),
                        ) { Text(item.label()) }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f)) {
                        Text(dueAt?.let { "\u622a\u6b62 ${it.formatDateTime()}" } ?: "\u9009\u62e9\u622a\u6b62\u65f6\u95f4")
                    }
                    if (dueAt != null) TextButton(onClick = { dueAt = null }) { Text("\u6e05\u9664") }
                }
                OutlinedTextField(
                    reminderMinutes,
                    { value -> reminderMinutes = value.filter { it.isDigit() }.take(5) },
                    label = { Text("\u63d0\u9192\u5468\u671f\uff08\u5206\u949f\uff09") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(title, desc, tags, reminderMinutes.toIntOrNull(), priority, dueAt) }) {
                Text(if (task == null) "\u52a0\u5165\u724c\u5806" else "\u4fdd\u5b58")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("\u53d6\u6d88") } },
    )

    if (showDatePicker) {
        val initialDateMillis = dueAt?.let {
            java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                .atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val dateState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pendingDate = dateState.selectedDateMillis?.let {
                        java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                    showTimePicker = pendingDate != null
                }) { Text("\u4e0b\u4e00\u6b65") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("\u53d6\u6d88") } },
        ) { DatePicker(state = dateState) }
    }

    if (showTimePicker) {
        val currentTime = dueAt?.let {
            java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalTime()
        } ?: java.time.LocalTime.now()
        val timeState = rememberTimePickerState(initialHour = currentTime.hour, initialMinute = currentTime.minute)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("\u9009\u62e9\u622a\u6b62\u65f6\u95f4") },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                Button(onClick = {
                    dueAt = pendingDate
                        ?.atTime(timeState.hour, timeState.minute)
                        ?.atZone(java.time.ZoneId.systemDefault())
                        ?.toInstant()
                        ?.toEpochMilli()
                    showTimePicker = false
                }) { Text("\u786e\u5b9a") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("\u53d6\u6d88") } },
        )
    }
}

@Composable
private fun BackupDialog(initialText: String, onDismiss: () -> Unit, onImport: (String) -> Unit) {
    var text by remember(initialText) { mutableStateOf(initialText) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialText.isBlank()) "\u5bfc\u5165\u5907\u4efd" else "\u5bfc\u51fa\u5907\u4efd") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                minLines = 8,
                label = { Text("JSON \u5907\u4efd\u5185\u5bb9") },
            )
        },
        confirmButton = {
            if (initialText.isBlank()) Button(onClick = { onImport(text) }) { Text("\u5bfc\u5165") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("\u5173\u95ed") } },
    )
}

@Composable
private fun cardBrush(cardStyleId: String, back: Boolean): Brush {
    val style = cardStyleOf(cardStyleId)
    val base = if (back) style.back.asColor() else style.front.asColor()
    val tint = lerp(base, style.accent.asColor(), if (back) 0.12f else 0.08f)
    return Brush.linearGradient(listOf(base, tint, base))
}

@Composable
private fun appColorScheme(template: com.slowly.manmanlai.ThemeTemplate) =
    if (template.id == "dark") {
        darkColorScheme(
            primary = template.colorScheme.accent.asColor(),
            secondary = template.colorScheme.cardBack.asColor(),
            primaryContainer = template.colorScheme.accent.asColor().copy(alpha = 0.24f),
            secondaryContainer = template.colorScheme.cardBack.asColor(),
            surfaceVariant = template.colorScheme.cardBack.asColor(),
            background = template.colorScheme.background.asColor(),
            surface = template.colorScheme.cardFront.asColor(),
            onPrimary = Color(0xFF101715),
            onSecondary = template.colorScheme.text.asColor(),
            onPrimaryContainer = template.colorScheme.text.asColor(),
            onSecondaryContainer = template.colorScheme.text.asColor(),
            onBackground = template.colorScheme.text.asColor(),
            onSurface = template.colorScheme.text.asColor(),
            onSurfaceVariant = template.colorScheme.text.asColor().copy(alpha = 0.76f),
        )
    } else {
        lightColorScheme(
            primary = template.colorScheme.accent.asColor(),
            secondary = template.colorScheme.cardBack.asColor(),
            primaryContainer = template.colorScheme.accent.asColor().copy(alpha = 0.2f),
            secondaryContainer = template.colorScheme.cardBack.asColor(),
            surfaceVariant = template.colorScheme.cardBack.asColor(),
            background = template.colorScheme.background.asColor(),
            surface = template.colorScheme.cardFront.asColor(),
            onPrimary = Color.White,
            onSecondary = template.colorScheme.text.asColor(),
            onPrimaryContainer = template.colorScheme.text.asColor(),
            onSecondaryContainer = template.colorScheme.text.asColor(),
            onBackground = template.colorScheme.text.asColor(),
            onSurface = template.colorScheme.text.asColor(),
            onSurfaceVariant = template.colorScheme.text.asColor().copy(alpha = 0.72f),
        )
    }

private fun appTypography(themeId: String): Typography {
    val displayFamily = when (themeId) {
        "fresh", "eye", "library", "sakura" -> FontFamily.Serif
        "dark", "studio" -> FontFamily.Monospace
        else -> FontFamily.SansSerif
    }
    val bodyFamily = FontFamily.SansSerif
    val utilityFamily = if (themeId == "dark" || themeId == "studio") FontFamily.Monospace else FontFamily.SansSerif
    return Typography(
        displayLarge = TextStyle(
            fontFamily = displayFamily,
            fontWeight = FontWeight.Black,
            fontSize = 48.sp,
            lineHeight = 56.sp,
            letterSpacing = 0.sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = displayFamily,
            fontWeight = FontWeight.Black,
            fontSize = 34.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp,
        ),
        headlineSmall = TextStyle(
            fontFamily = displayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            letterSpacing = 0.sp,
        ),
        titleLarge = TextStyle(
            fontFamily = displayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            lineHeight = 27.sp,
            letterSpacing = 0.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp,
        ),
        labelLarge = TextStyle(
            fontFamily = utilityFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = utilityFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = utilityFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.sp,
        ),
    )
}

private fun themeBackground(themeId: String): Brush = when (themeId) {
    "dark" -> Brush.verticalGradient(listOf(Color(0xFF111A17), Color(0xFF21322C), Color(0xFF0A0F0E)))
    "eye" -> Brush.linearGradient(listOf(Color(0xFFF9F6E8), Color(0xFFE5F0CF), Color(0xFFFDFBF1)))
    "coral" -> Brush.linearGradient(listOf(Color(0xFFFFF6E7), Color(0xFFFFCDBE), Color(0xFFFFF9F1)))
    "library" -> Brush.linearGradient(listOf(Color(0xFFFAEFD9), Color(0xFFE7C89F), Color(0xFFFFF7E8)))
    "ocean" -> Brush.linearGradient(listOf(Color(0xFFE8FCFF), Color(0xFFBFE8F2), Color(0xFFF7FEFF)))
    "sakura" -> Brush.linearGradient(listOf(Color(0xFFFFF4F8), Color(0xFFF4C9DA), Color(0xFFFFFBFD)))
    "studio" -> Brush.linearGradient(listOf(Color(0xFFF8F5EA), Color(0xFFD9E4EF), Color(0xFFFFFFFF)))
    else -> Brush.linearGradient(listOf(Color(0xFFF8F5E9), Color(0xFFD8F0E2), Color(0xFFFFFDF6)))
}

private fun themeImageRes(themeId: String): Int? = when (themeId) {
    "fresh" -> R.drawable.theme_bg_fresh
    "dark" -> R.drawable.theme_bg_dark
    "eye" -> R.drawable.theme_bg_eye
    "coral" -> R.drawable.theme_bg_coral
    "library" -> R.drawable.theme_bg_library
    "ocean" -> R.drawable.theme_bg_ocean
    "sakura" -> R.drawable.theme_bg_sakura
    "studio" -> R.drawable.theme_bg_studio
    else -> null
}

private fun hasThemeImage(themeId: String): Boolean = themeImageRes(themeId) != null

@Composable
private fun ThemeImageLayer(themeId: String, modifier: Modifier = Modifier) {
    val imageRes = themeImageRes(themeId) ?: return
    val scrim = if (themeId == "dark") Color.Black else Color.White
    Box(modifier) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            scrim.copy(alpha = if (themeId == "dark") 0.18f else 0.16f),
                            scrim.copy(alpha = 0.06f),
                            scrim.copy(alpha = if (themeId == "dark") 0.2f else 0.18f),
                        ),
                    ),
                ),
        )
    }
}

@Composable
private fun ThemeAtmosphere(themeId: String, modifier: Modifier = Modifier) {
    val accent = when (themeId) {
        "dark" -> Color(0xFF7DDEB0)
        "eye" -> Color(0xFF8FB866)
        "coral" -> Color(0xFFE85D45)
        "library" -> Color(0xFF9E7246)
        "ocean" -> Color(0xFF2794B2)
        "sakura" -> Color(0xFFD95C8A)
        "studio" -> Color(0xFF496F93)
        else -> Color(0xFF2F9C75)
    }
    val ink = if (themeId == "dark") Color.White else Color(0xFF2B211D)
    Canvas(modifier) {
        drawRect(
            color = accent.copy(alpha = if (themeId == "dark") 0.12f else 0.08f),
            topLeft = Offset(0f, size.height * 0.08f),
            size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.16f),
        )
        drawRect(
            color = accent.copy(alpha = if (themeId == "dark") 0.1f else 0.07f),
            topLeft = Offset(0f, size.height * 0.72f),
            size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.22f),
        )
        var x = -size.height
        while (x < size.width + size.height) {
            drawLine(
                color = ink.copy(alpha = if (themeId == "dark") 0.055f else 0.04f),
                start = Offset(x, size.height),
                end = Offset(x + size.height, 0f),
                strokeWidth = 2.5f,
            )
            x += 82f
        }
    }
}

@Composable
private fun ThemePreviewSwatch(themeId: String, accent: Color) {
    Box(
        Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(themeBackground(themeId))
            .border(1.dp, accent.copy(alpha = 0.55f), RoundedCornerShape(10.dp)),
    ) {
        themeImageRes(themeId)?.let { imageRes ->
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
            Box(Modifier.matchParentSize().background(accent.copy(alpha = 0.08f)))
        } ?: Canvas(Modifier.matchParentSize()) {
            drawRect(accent.copy(alpha = 0.18f), topLeft = Offset(0f, size.height * 0.55f))
            drawLine(accent.copy(alpha = 0.5f), Offset(0f, size.height), Offset(size.width, 0f), 3f)
            drawLine(Color.White.copy(alpha = if (themeId == "dark") 0.1f else 0.45f), Offset(size.width * 0.22f, 0f), Offset(size.width, size.height * 0.78f), 2f)
        }
    }
}

private fun com.slowly.manmanlai.TaskStatus.label(): String = when (this) {
    com.slowly.manmanlai.TaskStatus.TODO -> "\u8fdb\u884c\u4e2d"
    com.slowly.manmanlai.TaskStatus.DONE -> "\u5df2\u5b8c\u6210"
    com.slowly.manmanlai.TaskStatus.ARCHIVED -> "\u5df2\u6536\u85cf"
    com.slowly.manmanlai.TaskStatus.DELETED -> "\u56de\u6536\u7ad9"
}

private fun com.slowly.manmanlai.Priority.label(): String = when (this) {
    com.slowly.manmanlai.Priority.LOW -> "\u4f4e"
    com.slowly.manmanlai.Priority.NORMAL -> "\u666e\u901a"
    com.slowly.manmanlai.Priority.HIGH -> "\u9ad8"
}

private fun Long.takeCardCode(): String = if (this <= 0L) "0" else this.toString().takeLast(3).padStart(3, '0')

private fun iconFor(tab: Tab): String = when (tab) {
    Tab.Deck -> "\u2660"
    Tab.Pack -> "\u25c6"
    Tab.Summary -> "\u2713"
    Tab.Settings -> "\u25cb"
}
