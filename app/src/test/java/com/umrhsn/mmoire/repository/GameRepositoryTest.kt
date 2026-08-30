package com.umrhsn.mmoire.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.umrhsn.mmoire.models.UserImageList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class GameRepositoryTest {

    private val db: FirebaseFirestore = mock()
    private val storage: FirebaseStorage = mock()
    private lateinit var repository: GameRepository

    @BeforeEach
    fun setUp() {
        repository = GameRepository(db, storage)
    }

    @Test
    fun `getGame returns UserImageList when document exists`() = runTest {
        val gameName = "testGame"
        val userImageList = UserImageList(listOf("url1"))
        
        val collectionRef: CollectionReference = mock()
        val documentRef: DocumentReference = mock()
        val task: Task<DocumentSnapshot> = mock()
        val snapshot: DocumentSnapshot = mock()

        whenever(db.collection("games")).thenReturn(collectionRef)
        whenever(collectionRef.document(gameName)).thenReturn(documentRef)
        whenever(documentRef.get()).thenReturn(task)
        whenever(task.isComplete).thenReturn(true)
        whenever(task.exception).thenReturn(null)
        whenever(task.result).thenReturn(snapshot)
        whenever(snapshot.toObject(UserImageList::class.java)).thenReturn(userImageList)

        val result = repository.getGame(gameName)

        assertEquals(userImageList, result)
    }

    @Test
    fun `checkGameExists returns true when document exists`() = runTest {
        val gameName = "existingGame"
        
        val collectionRef: CollectionReference = mock()
        val documentRef: DocumentReference = mock()
        val task: Task<DocumentSnapshot> = mock()
        val snapshot: DocumentSnapshot = mock()

        whenever(db.collection("games")).thenReturn(collectionRef)
        whenever(collectionRef.document(gameName)).thenReturn(documentRef)
        whenever(documentRef.get()).thenReturn(task)
        whenever(task.isComplete).thenReturn(true)
        whenever(task.exception).thenReturn(null)
        whenever(task.result).thenReturn(snapshot)
        whenever(snapshot.exists()).thenReturn(true)

        val result = repository.checkGameExists(gameName)

        assertTrue(result)
    }
}
