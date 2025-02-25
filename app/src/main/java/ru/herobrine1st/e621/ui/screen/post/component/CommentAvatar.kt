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

package ru.herobrine1st.e621.ui.screen.post.component

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.material.placeholder
import ru.herobrine1st.e621.api.model.PostReduced

@Composable
fun CommentAvatar(
    avatarPost: PostReduced?,
    modifier: Modifier = Modifier,
    placeholder: Boolean = false
) {
    val url = avatarPost?.previewUrl ?: avatarPost?.croppedUrl
    var isPlaceholderActive by remember { mutableStateOf(true) }
    if (url != null) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .crossfade(true)
                .transformations(CircleCropTransformation()) // clip(CircleShape) is wrong (white/black zones are present)
                .build(),
            onSuccess = {
                isPlaceholderActive = false
            },
            onError = {
                isPlaceholderActive = false
            },
            modifier = modifier
                .clip(CircleShape) // For placeholder
                .placeholder(
                    isPlaceholderActive,
                    highlight = PlaceholderHighlight.fade()
                ),
            contentDescription = null
        )
    } else {
        Icon(
            Icons.Filled.AccountCircle,
            contentDescription = null,
            // If placeholder = true, there should be no avatarPost object provided (=null)
            modifier = modifier
                .clip(CircleShape) // For placeholder
                .placeholder(placeholder, highlight = PlaceholderHighlight.fade())
        )
    }
}