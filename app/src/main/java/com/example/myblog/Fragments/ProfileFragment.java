package com.example.myblog.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myblog.Activities.Home;
import com.example.myblog.Activities.RegisterActivity;
import com.example.myblog.Adapters.PostAdapter;
import com.example.myblog.Models.Post;
import com.example.myblog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    static int PReqCode = 1;
    static int REQUESTCODE = 1;
    Uri newUserPhotoUri;

    RecyclerView profilePostRV;
    CircleImageView currentUserPhoto;
    EditText currentUserName;
    TextView currentUserEmail;
    ImageView updateBtn;
    TextView updateText;

    PostAdapter postAdapter;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseUser currentUser;

    List<Post> currentUserPostList;


    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        final View fragmentView = inflater.inflate(R.layout.fragment_profile, container, false);

        currentUserPhoto = fragmentView.findViewById(R.id.profile_user_photo);
        currentUserEmail = fragmentView.findViewById(R.id.profile_user_email);
        currentUserName = fragmentView.findViewById(R.id.profile_user_name);
        updateText = fragmentView.findViewById(R.id.profile_text);

        currentUserName.setFocusable(false);
        currentUserName.setFocusableInTouchMode(false);
        currentUserName.setClickable(false);

        updateBtn = fragmentView.findViewById(R.id.profile_update_btn);

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateBtn.setImageResource(R.drawable.ic_done_black_24dp);
                updateText.setText("Finish");

                currentUserName.setFocusable(true);
                currentUserName.setFocusableInTouchMode(true);
                currentUserName.setClickable(true);

                currentUserPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (Build.VERSION.SDK_INT >= 22) {

                            checkAndRequestForPermission();
                        } else {

                            openGallery();
                        }


                    }
                });

                updateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        updateBtn.setImageResource(R.drawable.ic_settings_black_24dp);
                        updateText.setText("Update ");

                        currentUserName.setFocusable(false);
                        currentUserName.setFocusableInTouchMode(false);
                        currentUserName.setClickable(false);

                        String newDisplayName = currentUserName.getText().toString();

                        updateProfile(newDisplayName, newUserPhotoUri);

                    }
                });
            }
        });

        profilePostRV = fragmentView.findViewById(R.id.profile_postRV);
        profilePostRV.setLayoutManager(new LinearLayoutManager(getActivity()));
        profilePostRV.setHasFixedSize(true);


        profilePostRV.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

//                KEEP GOING !!!
//                AAAAAAAAAAAAAAAAA
//                LET'S GO

                return false;
            }
        });

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String currentUserId = currentUser.getUid();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("UserIdPosts").child(currentUserId);



        return fragmentView;
    }

    private void deletePost(String postKey) {

        DatabaseReference dPost = FirebaseDatabase.getInstance().getReference("Posts").child("postKey");
        DatabaseReference  dUserIdPost = FirebaseDatabase.getInstance().getReference("UserIdPosts").child(currentUser.getUid()).child("postKey");

        dPost.removeValue();
        dUserIdPost.removeValue();

        showMessage("Post deleted!");
    }

    private void updateProfile(final String name, Uri newUserPhotoUri) {

        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users_photos");

        final StorageReference imageFilePath = mStorage.child(newUserPhotoUri.getLastPathSegment());
        imageFilePath.putFile(newUserPhotoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .setPhotoUri(uri)
                                .build();

                        currentUser.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {

                                            showMessage("Update successfully");
                                            reloadFragment();
                                        }
                                    }
                                });
                    }
                });
            }
        });


    }

    private void showMessage(String message) {

        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    private void checkAndRequestForPermission() {

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Toast.makeText(getContext(), "Please accept for required permission", Toast.LENGTH_SHORT).show();
            } else {

                ActivityCompat.requestPermissions(getActivity(),
                                                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                                                    PReqCode);
            }
        } else {

            openGallery();
        }
    }

    private void updateUI() {

        Intent homeActivity = new Intent(getContext(), Home.class);
        startActivity(homeActivity);
    }

    private void openGallery() {

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUESTCODE);
    }

    private void reloadFragment() {

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (Build.VERSION.SDK_INT >= 26) {

            ft.setReorderingAllowed(false);
        }

        ft.detach(this).attach(this).commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        newUserPhotoUri = data.getData();
        currentUserPhoto.setImageURI(newUserPhotoUri);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (currentUser != null) {

            currentUserName.setText(currentUser.getDisplayName());
            currentUserEmail.setText(currentUser.getEmail());

            Glide.with(this).load(currentUser.getPhotoUrl()).into(currentUserPhoto);
        }


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                currentUserPostList = new ArrayList<>();

                for (DataSnapshot postsnap: dataSnapshot.getChildren()) {

                    Post post = postsnap.getValue(Post.class);
                    currentUserPostList.add(0, post);
                    profilePostRV.smoothScrollToPosition(0);
                }

                postAdapter = new PostAdapter(getActivity(), currentUserPostList);
                profilePostRV.setAdapter(postAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        updateUI();
    }
}
