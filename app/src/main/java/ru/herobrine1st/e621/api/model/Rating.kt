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

package ru.herobrine1st.e621.api.model

import androidx.annotation.StringRes
import com.fasterxml.jackson.annotation.JsonValue
import ru.herobrine1st.e621.R

enum class Rating(@StringRes val descriptionId: Int, val apiName: String) {
    SAFE(R.string.rating_safe, "safe"),
    QUESTIONABLE(R.string.rating_questionable, "questionable"),
    EXPLICIT(R.string.rating_explicit, "explicit");

    @JsonValue
    val shortName = apiName.substring(0, 1)

    companion object {
        val byAnyName: Map<String, Rating> = HashMap<String, Rating>().apply {
            values().forEach {
                put(it.apiName.lowercase(), it) // lowercase just in case
                put(it.shortName.lowercase(), it)
            }
        }
    }
}
