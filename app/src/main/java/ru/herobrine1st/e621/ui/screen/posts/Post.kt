package ru.herobrine1st.e621.ui.screen.posts

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.google.android.exoplayer2.MediaItem
import dagger.hilt.android.EntryPointAccessors
import ru.herobrine1st.e621.R
import ru.herobrine1st.e621.api.model.Post
import ru.herobrine1st.e621.module.LocalExoPlayer
import ru.herobrine1st.e621.ui.dialog.ContentDialog
import ru.herobrine1st.e621.ui.screen.posts.logic.PostViewModel
import ru.herobrine1st.e621.ui.screen.posts.logic.WikiResult
import ru.herobrine1st.e621.util.PostsSearchOptions
import ru.herobrine1st.e621.util.SearchOptions
import java.util.*

private const val TAG = "Post Screen"

@Composable
fun Post(
    initialPost: Post,
    @Suppress("UNUSED_PARAMETER") scrollToComments: Boolean, // TODO
    searchOptions: SearchOptions,
    onModificationClick: (PostsSearchOptions) -> Unit,
    onExit: () -> Unit,
    viewModel: PostViewModel = viewModel(
        factory = PostViewModel.provideFactory(
            EntryPointAccessors.fromActivity(
                LocalContext.current as Activity,
                PostViewModel.FactoryProvider::class.java
            ).provideFactory(), initialPost
        )
    )
) {
    val post = viewModel.post
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val wikiState = viewModel.wikiState
    if (wikiState != null) {
        ContentDialog(
            title = wikiState.title.replaceFirstChar { // Capitalize
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            },
            onDismissRequest = { viewModel.closeWikiPage() }) {
            LazyColumn(
                modifier = Modifier.height(screenHeight * 0.4f)
            ) {
                when (wikiState) {
                    is WikiResult.Loading -> items(50) {
                        Text(
                            "", modifier = Modifier
                                .fillMaxWidth()
                                .placeholder(true, highlight = PlaceholderHighlight.shimmer())
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    is WikiResult.Failure -> item {
                        Text(stringResource(R.string.wiki_load_failed))
                    }
                    is WikiResult.NotFound -> item {
                        Text(stringResource(R.string.not_found))
                    }
                    is WikiResult.Success -> item {
                        Text(wikiState.result.body)
                    }
                }
            }
        }
    }

    ExoPlayerHandler(post, onExit)
    LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
        item("media") {
            when {
                post.file.type.isVideo -> PostVideo(post.files.first { it.type.isVideo })
                post.file.type.isImage -> PostImage(
                    post = post,
                    aspectRatio = post.normalizedFile.aspectRatio,
                    openPost = null,
                    file = post.normalizedSample
                )
                else -> InvalidPost(text = stringResource(R.string.unsupported_post_type, post.file.type.extension))
            }
        }
        item("todo") {
            Text("TODO")
        }
        // TODO comments
        // TODO i18n
        tags(post, searchOptions, onModificationClick, onWikiClick = {
            viewModel.handleWikiClick(it)
        })
    }
}

fun LazyListScope.tags(
    post: Post,
    searchOptions: SearchOptions,
    onModificationClick: (PostsSearchOptions) -> Unit,
    onWikiClick: (String) -> Unit
) {
    tags("Artist", post.tags.artist, searchOptions, onModificationClick, onWikiClick)
    tags("Copyright", post.tags.copyright, searchOptions, onModificationClick, onWikiClick)
    tags("Character", post.tags.character, searchOptions, onModificationClick, onWikiClick)
    tags("Species", post.tags.species, searchOptions, onModificationClick, onWikiClick)
    tags("General", post.tags.general, searchOptions, onModificationClick, onWikiClick)
    tags("Lore", post.tags.lore, searchOptions, onModificationClick, onWikiClick)
    tags("Meta", post.tags.meta, searchOptions, onModificationClick, onWikiClick)
}

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.tags(
    title: String,
    tags: List<String>,
    searchOptions: SearchOptions,
    onModificationClick: (PostsSearchOptions) -> Unit,
    onWikiClick: (String) -> Unit
) {
    if (tags.isEmpty()) return
    stickyHeader("$title tags") { // TODO i18n
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(start = 8.dp)
                .height(ButtonDefaults.MinHeight)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colors.background,
                            MaterialTheme.colors.background.copy(alpha = 0f)
                        )
                    )
                )
        ) {
            Text(
                title,
                style = MaterialTheme.typography.h6,
                modifier = Modifier.weight(1f)
            ) // TODO i18n
        }
    }
    items(tags, key = { it }) {
        Tag(it, searchOptions, onModificationClick, onWikiClick)
    }
}

@Composable
fun Tag(
    tag: String,
    searchOptions: SearchOptions,
    onModificationClick: (PostsSearchOptions) -> Unit,
    onWikiClick: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Text(tag, modifier = Modifier.weight(1f))
        IconButton( // Add
            onClick = {
                onModificationClick(searchOptions.toBuilder { tags += tag })
            }
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = stringResource(R.string.add_tag_to_search)
            )
        }
        IconButton(
            onClick = {
                onModificationClick(searchOptions.toBuilder { tags += "-$tag" })
            }
        ) {
            Icon(
                Icons.Default.Remove,
                contentDescription = stringResource(R.string.exclude_tag_from_search)
            )
        }
        IconButton(
            onClick = {
                onWikiClick(tag)
            }
        ) {
            Icon(
                Icons.Default.Help,
                contentDescription = stringResource(R.string.tag_view_wiki)
            )
        }
    }
}


// Set media item only on first composition in this scope (likely it is a navigation graph)
// Clear media item on exit
// Like DisposableEffect, but in scope of a graph
// Cannot use RememberObserver because onForgotten is triggered on decomposition even if rememberSaveable is used
@Composable
fun ExoPlayerHandler(post: Post, onExit: () -> Unit) {
    val exoPlayer = LocalExoPlayer.current
    var mediaItemIsSet by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (mediaItemIsSet) return@LaunchedEffect
        if (post.file.type.isNotVideo) return@LaunchedEffect
        exoPlayer.setMediaItem(MediaItem.fromUri(post.files.first { it.type.isVideo }.urls.first()))
        exoPlayer.prepare()
        mediaItemIsSet = true
    }

    BackHandler {
        exoPlayer.clearMediaItems()
        onExit()
    }
}