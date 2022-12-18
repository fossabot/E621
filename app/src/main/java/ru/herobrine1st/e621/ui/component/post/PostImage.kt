/*
 * ru.herobrine1st.e621 is an android client for https://e621.net and https://e926.net
 * Copyright (C) 2022  HeroBrine1st Erquilenne <project-e621-android@herobrine1st.ru>
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

package ru.herobrine1st.e621.ui.component.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.material.placeholder
import ru.herobrine1st.e621.R
import ru.herobrine1st.e621.api.model.NormalizedFile
import ru.herobrine1st.e621.api.model.Post
import ru.herobrine1st.e621.ui.component.OutlinedChip
import ru.herobrine1st.e621.ui.screen.posts.InvalidPost


@Composable
fun PostImage(
    post: Post,
    openPost: (() -> Unit)?,
    file: NormalizedFile,
    initialAspectRatio: Float = file.aspectRatio
) {
    var isPlaceholderActive by remember { mutableStateOf(true) }
    var aspectRatio by remember { mutableStateOf(initialAspectRatio) }
    if (aspectRatio <= 0) {
        InvalidPost(stringResource(R.string.invalid_post_server_error))
        return
    }

    val modifier = if (openPost == null) Modifier else Modifier.clickable {
        openPost()
    }
    val painter = rememberImagePainter(file.urls.first())

    Box(contentAlignment = Alignment.TopStart) {
        AsyncImage(
            model = file.urls.first(),
            modifier = Modifier
                .clickable(openPost != null) {
                    openPost?.invoke()
                }
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .placeholder(isPlaceholderActive, highlight = PlaceholderHighlight.fade()),
            onSuccess = {
                isPlaceholderActive = false
                aspectRatio = it.result.drawable.run { intrinsicWidth.toFloat() / intrinsicHeight }
            },
            onError = {
                isPlaceholderActive = false
            },
            contentDescription = remember(post.id) { post.tags.all.joinToString(" ") },
            contentScale = ContentScale.Fit /// // Drop 1-2 pixels (on my device)
        )
        if (post.file.type.isNotImage) OutlinedChip( // TODO
            modifier = Modifier.offset(x = 10.dp, y = 10.dp),
            backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.3f)
        ) {
            Text(post.file.type.extension)
        }
    }
}

