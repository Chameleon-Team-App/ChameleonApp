package com.example.mainchameleon.ui.userProfile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainchameleon.R
import com.example.mainchameleon.ui.journal.JournalAdapter
import com.example.mainchameleon.ui.journal.JournalEntry
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class FriendProfileFragment : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var bioTextView: TextView
    private lateinit var journalRecyclerView: RecyclerView
    private lateinit var journalAdapter: JournalAdapter
    private lateinit var database: DatabaseReference
    private var friendId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            friendId = it.getString("friendId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friend_profile, container, false)

        profileImageView = view.findViewById(R.id.profile_image)
        usernameTextView = view.findViewById(R.id.username_text)
        bioTextView = view.findViewById(R.id.bio_text)
        journalRecyclerView = view.findViewById(R.id.journal_recycler_view)

        journalRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        journalAdapter = JournalAdapter()
        journalRecyclerView.adapter = journalAdapter

        database = FirebaseDatabase.getInstance().reference

        loadFriendProfile()

        return view
    }

    private fun loadFriendProfile() {
        if (friendId == null) return

        database.child("users").child(friendId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.child("Username").value.toString()
                val bio = snapshot.child("bio").value.toString()
                val profilePictureUrl = snapshot.child("profilePictureUrl").value.toString()

                usernameTextView.text = username
                bioTextView.text = bio
                if (profilePictureUrl.isNotEmpty()) {
                    Picasso.get().load(profilePictureUrl).placeholder(R.drawable.default_profile).into(profileImageView)
                } else {
                    profileImageView.setImageResource(R.drawable.default_profile)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        loadFriendJournals()
    }

    private fun loadFriendJournals() {
        if (friendId == null) return

        database.child("journals").child(friendId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val journals = mutableListOf<JournalEntry>()
                for (journalSnapshot in snapshot.children) {
                    val journal = journalSnapshot.getValue(JournalEntry::class.java)
                    journal?.let { journals.add(it) }
                }
                journalAdapter.submitList(journals)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}
