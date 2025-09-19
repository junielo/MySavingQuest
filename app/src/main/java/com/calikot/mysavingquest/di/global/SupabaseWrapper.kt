package com.calikot.mysavingquest.di.global

import com.calikot.mysavingquest.conn.Connections.supabase
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wrapper for Supabase queries, providing standard error handling and user-based filtering.
 */
@Singleton
class SupabaseWrapper @Inject constructor(
    val userAuthState: UserAuthState
) {
    /**
     * Fetches data owned by the currently logged-in user from the specified table.
     * @param tableName The name of the table to query.
     * @return Result with a non-null list of items or an error.
     */
    suspend inline fun <reified T : Any> getOwnListData(
        tableName: String
    ): Result<List<T>> {
        val userId = userAuthState.getUserLoggedIn()?.user?.id
        if (userId == null) {
            return Result.failure(IllegalStateException("User not logged in."))
        }
        return try {
            val result = supabase.from(tableName)
                .select(columns = io.github.jan.supabase.postgrest.query.Columns.ALL) {
                    filter { eq("user_id", userId) }
                }
                .decodeList<T>()
            if (result.isEmpty()) {
                Result.failure(NoSuchElementException("No data found for user."))
            } else {
                Result.success(result)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend inline fun <reified T : Any> getOwnSingleData(
        tableName: String,
        id: String
    ): Result<T> {
        val userId = userAuthState.getUserLoggedIn()?.user?.id
        if (userId == null) {
            return Result.failure(IllegalStateException("User not logged in."))
        }
        return try {
            val result = supabase.from(tableName)
                .select(columns = io.github.jan.supabase.postgrest.query.Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<T>()
            if (result == null) {
                Result.failure(NoSuchElementException("No data found for user with the given id."))
            } else {
                Result.success(result)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Inserts a new item owned by the currently logged-in user into the specified table.
     * @param tableName The name of the table to insert into.
     * @param data The data to insert. Must include user_id field.
     * @return Result with the inserted item or an error.
     */
    suspend inline fun <reified T : Any> addOwnData(
        tableName: String,
        data: T
    ): Result<T> {
        val userId = userAuthState.getUserLoggedIn()?.user?.id
        if (userId == null) {
            return Result.failure(IllegalStateException("User not logged in."))
        }
        return try {
            val result = supabase.from(tableName)
                .insert(data)
                .decodeSingleOrNull<T>()
            if (result == null) {
                return Result.failure(Exception("Insert failed"))
            } else {
                Result.success(result)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates an item owned by the currently logged-in user in the specified table.
     * @param tableName The name of the table to update.
     * @param id The id of the item to update.
     * @param data The new data for the item.
     * @return Result with the updated item or an error.
     */
    suspend inline fun <reified T : Any> updateOwnData(
        tableName: String,
        id: Int?,
        data: T
    ): Result<T> {
        val userId = userAuthState.getUserLoggedIn()?.user?.id
        if (userId == null) {
            return Result.failure(IllegalStateException("User not logged in."))
        }
        if (id == null) {
            return Result.failure(IllegalArgumentException("ID cannot be null."))
        }
        return try {
            val result = supabase.from(tableName)
                .update(data) {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<T>()
            if (result == null) {
                return Result.failure(Exception("Insert failed"))
            } else {
                Result.success(result)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes an item owned by the currently logged-in user from the specified table.
     * @param tableName The name of the table to delete from.
     * @param id The id of the item to delete.
     * @return Result with true if deleted, or an error.
     */
    suspend fun deleteOwnData(
        tableName: String,
        id: Int?
    ): Result<Boolean> {
        val userId = userAuthState.getUserLoggedIn()?.user?.id
        if (userId == null) {
            return Result.failure(IllegalStateException("User not logged in."))
        }
        if (id == null) {
            return Result.failure(IllegalArgumentException("ID cannot be null."))
        }
        return try {
            supabase.from(tableName)
                .delete {
                    filter {
                        eq("id", id)
                        eq("user_id", userId)
                    }
                }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}