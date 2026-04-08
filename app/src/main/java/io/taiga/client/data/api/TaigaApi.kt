package io.taiga.client.data.api

import io.taiga.client.data.models.IssueDetail
import io.taiga.client.data.models.IssueSummary
import io.taiga.client.data.models.ProjectSummary
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TaigaApi {
    @GET("projects")
    suspend fun projects(@Query("slight") slight: Boolean = true): List<ProjectSummary>

    @GET("issues")
    suspend fun issues(@Query("project") projectId: Long): List<IssueSummary>

    @GET("issues/{issueId}")
    suspend fun issue(@Path("issueId") issueId: Long): IssueDetail
}
