package lb.edu.ul.mobileproject.drawer;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import lb.edu.ul.mobileproject.R;
public class ContactFragment extends Fragment {

    private EditText  editTextMessage, editSubjectMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contact, container, false);

        ImageView messageCallIcon = rootView.findViewById(R.id.message_icon_id);
        TextView messageCallText = rootView.findViewById(R.id.message_text_id);
        editTextMessage = rootView.findViewById(R.id.contact_message);
        editSubjectMessage = rootView.findViewById(R.id.subject_email);
        TextView buttonSubmit = rootView.findViewById(R.id.contact_submit);
        View.OnClickListener messageClickListener = v -> openMessageApp();
        messageCallIcon.setOnClickListener(messageClickListener);
        messageCallText.setOnClickListener(messageClickListener);

        buttonSubmit.setOnClickListener(v -> sendEmail());

        return rootView;
    }

    private void openMessageApp() {
        String phoneNumber = "81057262";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phoneNumber));
        intent.putExtra("sms_body", "Hello, I need support regarding...");
        startActivity(intent);
    }

    private void sendEmail() {

        String description = editTextMessage.getText().toString().trim();
        String subject = editSubjectMessage.getText().toString().trim();

        if ( description.isEmpty() || subject.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"mhmdam12.almhmd@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, "From: " +  "\n\nMessage:\n" + description);

        try {
            startActivity(Intent.createChooser(intent, "Send Email via..."));
        } catch (Exception e) {
            Toast.makeText(getActivity(), "No email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
}