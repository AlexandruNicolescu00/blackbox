package com.example.blackbox.domain.use_case

import com.example.blackbox.common.Resource
import com.example.blackbox.data.remote.dto.TaggedDataBlock
import com.example.blackbox.domain.repository.IOTARepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class GetData @Inject constructor(
    private val repository: IOTARepository
) {

    operator fun invoke(blockId: String): Flow<Resource<TaggedDataBlock>> = flow {
        try {
            emit(Resource.Loading())
            val taggedDataBlock = repository.getData(blockId = blockId)
            emit(Resource.Success(taggedDataBlock))
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach ani node. Check your internet connection"))
        }
    }
}