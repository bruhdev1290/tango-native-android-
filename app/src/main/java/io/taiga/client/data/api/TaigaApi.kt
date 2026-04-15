package io.taiga.client.data.api

import io.taiga.client.data.models.IssueDetail
import io.taiga.client.data.models.IssueSummary
import io.taiga.client.data.models.ProjectSummary
import io.taiga.client.data.models.SprintSummary
import io.taiga.client.data.models.TaskSummary
import io.taiga.client.data.models.UserMe
import io.taiga.client.data.models.UserStorySummary
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TaigaApi {
    @GET("projects")
    suspend fun projects(
        @Query("slight") slight: Boolean = true,
        @Query("member") member: Long? = null,
    ): List<ProjectSummary>

    @GET("issues")
    suspend fun issues(
        @Query("project") projectId: Long? = null,
        @Query("assigned_to") assignedTo: Long? = null,
    ): List<IssueSummary>

    @GET("issues/{issueId}")
    suspend fun issue(@Path("issueId") issueId: Long): IssueDetail

    @GET("users/me")
    suspend fun me(): UserMe

    @GET("userstories")
    suspend fun userStories(
        @Query("project") projectId: Long? = null,
        @Query("assigned_to") assignedTo: Long? = null,
        @Query("milestone") milestoneId: Long? = null,
    ): List<UserStorySummary>

    @GET("tasks")
    suspend fun tasks(
        @Query("project") projectId: Long? = null,
        @Query("assigned_to") assignedTo: Long? = null,
        @Query("user_story") userStoryId: Long? = null,
    ): List<TaskSummary>

    @GET("milestones")
    suspend fun milestones(@Query("project") projectId: Long): List<SprintSummary>

    @POST("userstories")
    suspend fun createUserStory(@Body body: Map<String, @JvmSuppressWildcards Any>): UserStorySummary

    @PATCH("userstories/{id}")
    suspend fun updateUserStory(
        @Path("id") id: Long,
        @Body body: Map<String, @JvmSuppressWildcards Any>,
    ): UserStorySummary

    @DELETE("userstories/{id}")
    suspend fun deleteUserStory(@Path("id") id: Long)

    @POST("tasks")
    suspend fun createTask(@Body body: Map<String, @JvmSuppressWildcards Any>): TaskSummary

    @PATCH("tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: Long,
        @Body body: Map<String, @JvmSuppressWildcards Any>,
    ): TaskSummary

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Long)

    @POST("issues")
    suspend fun createIssue(@Body body: Map<String, @JvmSuppressWildcards Any>): IssueSummary

    @PATCH("issues/{id}")
    suspend fun updateIssue(
        @Path("id") id: Long,
        @Body body: Map<String, @JvmSuppressWildcards Any>,
    ): IssueSummary

    @DELETE("issues/{id}")
    suspend fun deleteIssue(@Path("id") id: Long)
}
