/*
 * This file is part of ru.herobrine1st.e621.
 *
 * ru.herobrine1st.e621 is an android client for https://e621.net
 * Copyright (C) 2022-2023 HeroBrine1st Erquilenne <project-e621-android@herobrine1st.ru>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ru.herobrine1st.e621.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.LocalOwnersProvider
import androidx.navigation.compose.currentBackStackEntryAsState
import ru.herobrine1st.e621.R
import ru.herobrine1st.e621.ui.screen.Screen

@Composable
fun MainScaffold(navController: NavHostController, scaffoldState: ScaffoldState, onOpenBlacklistDialog: () -> Unit) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val screen by remember { derivedStateOf { Screen.byRoute[navBackStackEntry?.destination?.route] } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Emulate NavHost over whole screen (Its animations, I mean)
                    Crossfade(screen) {
                        Text(stringResource(it?.title ?: R.string.app_name))
                    }
                },
                backgroundColor = MaterialTheme.colors.primarySurface,
                elevation = 12.dp,
                actions = {
                    val saveableStateHolder = rememberSaveableStateHolder()
                    Crossfade(navBackStackEntry to screen) {
                        it.first?.LocalOwnersProvider(saveableStateHolder = saveableStateHolder) {
                            it.second?.appBarActions?.invoke(this, navController)
                        }
                    }
                    ActionBarMenu(navController, onOpenBlacklistDialog)
                }
            )
        },
        scaffoldState = scaffoldState,
        floatingActionButton = {
            val saveableStateHolder = rememberSaveableStateHolder()
            Crossfade(navBackStackEntry to screen) {
                it.first?.LocalOwnersProvider(saveableStateHolder = saveableStateHolder) {
                    it.second?.floatingActionButton?.invoke()
                }
            }
        }
    ) {
        Surface(
            color = MaterialTheme.colors.background,
            modifier = Modifier.padding(it)
        ) {
            Navigator(navController)
        }
    }
}