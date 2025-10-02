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
     * Retrieves a list of items from the specified table that are owned by the currently logged-in user.
     *
     * @param tableName The name of the table to query.
     * @return [Result] containing a non-empty list of items of type [T] if successful, or an error if the user is not logged in or no data is found.
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

    /**
     * Retrieves a single item from the specified table that is owned by the currently logged-in user, optionally filtered by ID.
     *
     * @param tableName The name of the table to query.
     * @param id Optional ID of the item to retrieve. If null, only user_id is used for filtering.
     * @return [Result] containing the item of type [T] if found, or an error if not found or the user is not logged in.
     */
    suspend inline fun <reified T : Any> getOwnSingleData(
        tableName: String,
        id: String? = null
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
                        if (id != null) { eq("id", id) }
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
     * Inserts a new item into the specified table for the currently logged-in user.
     *
     * @param tableName The name of the table to insert into.
     * @param data The data to insert. Must include a user_id field matching the logged-in user.
     * @return [Result] containing the inserted item of type [T] if successful, or an error if the user is not logged in or the insert fails.
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

    suspend inline fun <reified T : Any> addBulkOwnData(
        tableName: String,
        data: List<T>
    ): Result<Boolean> {
        val userId = userAuthState.getUserLoggedIn()?.user?.id
        if (userId == null) {
            return Result.failure(IllegalStateException("User not logged in."))
        }
        return try {
            supabase.from(tableName)
                .insert(data)
            Result.success(true)
        } catch (e: Exception) {
            println("qwerty - Error inserting bulk data: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Updates an existing item in the specified table that is owned by the currently logged-in user.
     *
     * @param tableName The name of the table to update.
     * @param id The ID of the item to update. Must not be null.
     * @param data The new data for the item.
     * @return [Result] containing the updated item of type [T] if successful, or an error if the user is not logged in, the ID is null, or the update fails.
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
                    select()
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
     * Inserts or updates an item in the specified table for the currently logged-in user (upsert operation).
     *
     * @param tableName The name of the table to upsert into.
     * @param data The data to upsert. Must include a user_id field matching the logged-in user.
     * @return [Result] containing the upserted item of type [T] if successful, or an error if the user is not logged in or the upsert fails.
     */
    suspend inline fun <reified T : Any> upsertOwnData(
        tableName: String,
        data: T
    ): Result<T> {
        val userId = userAuthState.getUserLoggedIn()?.user?.id
        if (userId == null) {
            return Result.failure(IllegalStateException("User not logged in."))
        }
        return try {
            val result = supabase.from(tableName)
                .upsert(data)
                .decodeSingleOrNull<T>()
            if (result == null) {
                return Result.failure(Exception("Upsert failed"))
            } else {
                Result.success(result)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes an item from the specified table that is owned by the currently logged-in user.
     *
     * @param tableName The name of the table to delete from.
     * @param id The ID of the item to delete. Must not be null.
     * @return [Result] containing true if the item was deleted, or an error if the user is not logged in, the ID is null, or the delete fails.
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