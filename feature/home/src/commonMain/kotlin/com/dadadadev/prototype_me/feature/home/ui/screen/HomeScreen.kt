package com.dadadadev.prototype_me.feature.home.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadadev.prototype_me.feature.home.presentation.HomeViewModel
import com.dadadadev.prototype_me.feature.home.presentation.contract.HomeIntent
import com.dadadadev.prototype_me.feature.home.presentation.contract.HomeSideEffect
import com.dadadadev.prototype_me.feature.home.presentation.contract.HomeState
import com.dadadadev.prototype_me.feature.home.ui.dimens.HomeDimens
import com.dadadadev.prototype_me.feature.home.ui.theme.HomeStrings
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun HomeScreen(
    onNavigateToBoard: (boardId: String, userId: String) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is HomeSideEffect.NavigateToBoard -> onNavigateToBoard(effect.boardId, effect.userId)
        }
    }

    HomeScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
private fun HomeScreenContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
) {
    val uiModel = state.toUiModel()
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = HomeDimens.MAX_CONTENT_WIDTH_DP.dp)
                .padding(horizontal = HomeDimens.SCREEN_HORIZONTAL_PADDING_DP.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HomeTitle()
            Spacer(modifier = Modifier.height(HomeDimens.HEADER_TO_CARD_SPACING_DP.dp))

            HomeActionsCard(
                state = state,
                uiModel = uiModel,
                onIntent = onIntent,
            )

            Spacer(modifier = Modifier.height(HomeDimens.FOOTER_TOP_SPACING_DP.dp))
            HomeShareHint()
        }
    }
}

@Composable
private fun HomeTitle() {
    val colors = MaterialTheme.colorScheme
    Text(
        text = HomeStrings.APP_TITLE,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = colors.onBackground,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(HomeDimens.TITLE_BOTTOM_SPACING_DP.dp))
    Text(
        text = HomeStrings.APP_SUBTITLE,
        fontSize = 13.sp,
        color = colors.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun HomeActionsCard(
    state: HomeState,
    uiModel: HomeUiModel,
    onIntent: (HomeIntent) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colors.surface,
                shape = RoundedCornerShape(HomeDimens.CARD_CORNER_RADIUS_DP.dp),
            )
            .border(
                border = BorderStroke(HomeDimens.CARD_BORDER_WIDTH_DP.dp, colors.outline),
                shape = RoundedCornerShape(HomeDimens.CARD_CORNER_RADIUS_DP.dp),
            )
            .padding(HomeDimens.CARD_PADDING_DP.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HomeDimens.CARD_CONTENT_SPACING_DP.dp),
    ) {
        if (!state.errorMessage.isNullOrBlank()) {
            Text(
                text = state.errorMessage,
                fontSize = 13.sp,
                color = colors.error,
                textAlign = TextAlign.Center,
            )
        }

        if (state.isAuthenticating) {
            Text(
                text = HomeStrings.AUTH_IN_PROGRESS,
                fontSize = 12.sp,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        AnimatedContent(
            targetState = state.isJoinMode,
            label = "HomeJoinMode",
        ) { isJoinMode ->
            if (isJoinMode) {
                HomeJoinActions(
                    state = state,
                    joinEnabled = uiModel.isJoinEnabled,
                    onIntent = onIntent,
                )
            } else {
                HomeDefaultActions(
                    isBusy = uiModel.isBusy,
                    createLabel = uiModel.createButtonLabel,
                    onIntent = onIntent,
                )
            }
        }
    }
}

@Composable
private fun HomeDefaultActions(
    isBusy: Boolean,
    createLabel: String,
    onIntent: (HomeIntent) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val buttonShape = RoundedCornerShape(HomeDimens.BUTTON_CORNER_RADIUS_DP.dp)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HomeDimens.CARD_CONTENT_SPACING_DP.dp),
    ) {
        Button(
            onClick = { onIntent(HomeIntent.OnCreateBoard) },
            enabled = !isBusy,
            modifier = Modifier
                .fillMaxWidth()
                .height(HomeDimens.BUTTON_HEIGHT_DP.dp),
            shape = buttonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
                disabledContainerColor = colors.outline,
                disabledContentColor = colors.onSurfaceVariant,
            ),
        ) {
            Text(
                text = createLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        OutlinedButton(
            onClick = { onIntent(HomeIntent.OnOpenJoinMode) },
            enabled = !isBusy,
            modifier = Modifier
                .fillMaxWidth()
                .height(HomeDimens.BUTTON_HEIGHT_DP.dp),
            shape = buttonShape,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.secondary),
            border = BorderStroke(HomeDimens.CARD_BORDER_WIDTH_DP.dp, colors.outline),
        ) {
            Text(
                text = HomeStrings.JOIN_BOARD,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun HomeJoinActions(
    state: HomeState,
    joinEnabled: Boolean,
    onIntent: (HomeIntent) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val buttonShape = RoundedCornerShape(HomeDimens.BUTTON_CORNER_RADIUS_DP.dp)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HomeDimens.CARD_CONTENT_SPACING_DP.dp),
    ) {
        Text(
            text = HomeStrings.ENTER_BOARD_ID,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = colors.onSurface,
        )

        OutlinedTextField(
            value = state.joinBoardId,
            onValueChange = { onIntent(HomeIntent.OnJoinBoardIdChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = HomeStrings.BOARD_ID_PLACEHOLDER,
                    color = colors.onSurfaceVariant,
                    fontSize = 13.sp,
                )
            },
            singleLine = true,
            shape = buttonShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.outline,
                cursorColor = colors.primary,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { onIntent(HomeIntent.OnSubmitJoinBoard) }),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HomeDimens.BUTTONS_ROW_SPACING_DP.dp),
        ) {
            OutlinedButton(
                onClick = { onIntent(HomeIntent.OnCloseJoinMode) },
                modifier = Modifier
                    .weight(1f)
                    .height(HomeDimens.BUTTON_HEIGHT_DP.dp),
                shape = buttonShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.onSurfaceVariant),
                border = BorderStroke(HomeDimens.CARD_BORDER_WIDTH_DP.dp, colors.outline),
            ) {
                Text(text = HomeStrings.BACK, fontSize = 13.sp)
            }

            Button(
                onClick = { onIntent(HomeIntent.OnSubmitJoinBoard) },
                enabled = joinEnabled,
                modifier = Modifier
                    .weight(1f)
                    .height(HomeDimens.BUTTON_HEIGHT_DP.dp),
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                    disabledContainerColor = colors.outline,
                    disabledContentColor = colors.onSurfaceVariant,
                ),
            ) {
                Text(
                    text = HomeStrings.CONNECT,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun HomeShareHint() {
    val colors = MaterialTheme.colorScheme
    Text(
        text = HomeStrings.SHARE_HINT,
        fontSize = 12.sp,
        color = colors.onSurfaceVariant,
        textAlign = TextAlign.Center,
        lineHeight = HomeDimens.FOOTER_LINE_HEIGHT_SP.sp,
    )
}

private data class HomeUiModel(
    val isBusy: Boolean,
    val isJoinEnabled: Boolean,
    val createButtonLabel: String,
)

private fun HomeState.toUiModel(): HomeUiModel {
    val busy = isCreatingBoard || isAuthenticating
    val createLabel = when {
        isCreatingBoard -> HomeStrings.CREATE_BOARD_LOADING
        isAuthenticating -> HomeStrings.PREPARING
        else -> HomeStrings.CREATE_BOARD
    }

    return HomeUiModel(
        isBusy = busy,
        isJoinEnabled = joinBoardId.isNotBlank() && !busy,
        createButtonLabel = createLabel,
    )
}
