package com.example.garoon_pre.app.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.garoon_pre.feature.auth.presentation.gate.GateState
import com.example.garoon_pre.feature.auth.presentation.gate.GateViewModel
import com.example.garoon_pre.feature.auth.presentation.login.LoginRoute
import com.example.garoon_pre.feature.availability.presentation.AvailabilityRoute
import com.example.garoon_pre.feature.board.presentation.create.CreateBoardPostRoute
import com.example.garoon_pre.feature.board.presentation.create.EditBoardPostRoute
import com.example.garoon_pre.feature.board.presentation.detail.BoardDetailRoute
import com.example.garoon_pre.feature.board.presentation.list.BoardListRoute
import com.example.garoon_pre.feature.board.presentation.postlist.BoardPostListRoute
import com.example.garoon_pre.feature.home.presentation.HomeMenuRoute
import com.example.garoon_pre.feature.schedule.presentation.create.CreateScheduleRoute
import com.example.garoon_pre.feature.schedule.presentation.create.EditScheduleRoute
import com.example.garoon_pre.feature.schedule.presentation.detail.ScheduleDetailRoute
import com.example.garoon_pre.feature.schedule.presentation.list.ScheduleListRoute

private const val ROUTE_GATE = "gate"
private const val ROUTE_LOGIN = "login"
private const val ROUTE_HOME_MENU = "home_menu"

private const val ROUTE_SCHEDULE_LIST = "schedule_list"
private const val ROUTE_SCHEDULE_DETAIL =
    "schedule_detail/{id}?occurrenceStartAt={occurrenceStartAt}&occurrenceEndAt={occurrenceEndAt}"
private const val ROUTE_SCHEDULE_CREATE = "schedule_create"
private const val ROUTE_SCHEDULE_EDIT = "schedule_edit/{id}"

private const val ROUTE_BOARD_LIST = "board_list"
private const val ROUTE_BOARD_POST_LIST = "board_posts/{categoryId}"
private const val ROUTE_BOARD_DETAIL = "board_detail/{postId}"
private const val ROUTE_BOARD_CREATE = "board_create/{categoryId}"
private const val ROUTE_BOARD_EDIT = "board_edit/{postId}"

private const val ROUTE_AVAILABILITY = "availability"

@Composable
fun AppNav() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ROUTE_GATE
    ) {
        composable(ROUTE_GATE) {
            val viewModel: GateViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()

            LaunchedEffect(state) {
                when (state) {
                    GateState.Loading -> Unit
                    GateState.LoggedIn -> {
                        navController.navigate(ROUTE_HOME_MENU) {
                            popUpTo(ROUTE_GATE) { inclusive = true }
                        }
                    }

                    GateState.LoggedOut -> {
                        navController.navigate(ROUTE_LOGIN) {
                            popUpTo(ROUTE_GATE) { inclusive = true }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        composable(ROUTE_LOGIN) {
            LoginRoute(
                onLoginDone = {
                    navController.navigate(ROUTE_HOME_MENU) {
                        popUpTo(ROUTE_LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(ROUTE_HOME_MENU) {
            HomeMenuRoute(
                onOpenScheduleList = {
                    navController.navigate(ROUTE_SCHEDULE_LIST)
                },
                onOpenBoardList = {
                    navController.navigate(ROUTE_BOARD_LIST)
                },
                onOpenAvailability = {
                    navController.navigate(ROUTE_AVAILABILITY)
                },
                onLogout = {
                    navController.navigate(ROUTE_LOGIN) {
                        popUpTo(ROUTE_HOME_MENU) { inclusive = true }
                    }
                }
            )
        }

        composable(ROUTE_SCHEDULE_LIST) {
            ScheduleListRoute(
                onBack = { navController.popBackStack() },
                onOpenDetail = { encodedId, encodedStartAt, encodedEndAt ->
                    navController.navigate(
                        "schedule_detail/$encodedId?occurrenceStartAt=$encodedStartAt&occurrenceEndAt=$encodedEndAt"
                    )
                },
                onOpenCreate = {
                    navController.navigate(ROUTE_SCHEDULE_CREATE)
                },
                onLoggedOut = {
                    navController.navigate(ROUTE_LOGIN) {
                        popUpTo(ROUTE_SCHEDULE_LIST) { inclusive = true }
                    }
                }
            )
        }

        composable(ROUTE_SCHEDULE_DETAIL) {
            ScheduleDetailRoute(
                onBack = { navController.popBackStack() },
                onOpenEdit = { encodedId ->
                    navController.navigate("schedule_edit/$encodedId")
                }
            )
        }

        composable(ROUTE_SCHEDULE_EDIT) {
            EditScheduleRoute(
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(ROUTE_SCHEDULE_CREATE) {
            CreateScheduleRoute(
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(ROUTE_BOARD_LIST) {
            BoardListRoute(
                onBack = { navController.popBackStack() },
                onOpenCategory = { categoryId ->
                    navController.navigate("board_posts/${Uri.encode(categoryId)}")
                }
            )
        }

        composable(ROUTE_BOARD_POST_LIST) {
            BoardPostListRoute(
                onBack = { navController.popBackStack() },
                onOpenCreate = { categoryId ->
                    navController.navigate("board_create/${Uri.encode(categoryId)}")
                },
                onOpenDetail = { postId ->
                    navController.navigate("board_detail/${Uri.encode(postId)}")
                }
            )
        }

        composable(ROUTE_BOARD_DETAIL) {
            BoardDetailRoute(
                onBack = { navController.popBackStack() },
                onOpenEdit = { postId ->
                    navController.navigate("board_edit/${Uri.encode(postId)}")
                }
            )
        }

        composable(ROUTE_BOARD_CREATE) {
            CreateBoardPostRoute(
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(ROUTE_BOARD_EDIT) {
            EditBoardPostRoute(
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }


        composable(ROUTE_AVAILABILITY) {
            AvailabilityRoute(
                onBack = { navController.popBackStack() },
                onOpenDetail = { encodedId, encodedStartAt, encodedEndAt ->
                    navController.navigate(
                        "schedule_detail/$encodedId?occurrenceStartAt=$encodedStartAt&occurrenceEndAt=$encodedEndAt"
                    )
                }
            )
        }
    }
}