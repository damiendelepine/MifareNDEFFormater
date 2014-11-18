package fr.bactech.mifarendefformater;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private Tag tag; 
	private NdefFormatable card_to_format;
	private TextView tv1;
	private NfcAdapter mNfcAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		tv1 = (TextView)findViewById(R.id.tv1);
		nfc_init();	// initialisation du NFC
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		try {
			tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG); // on récupèrel'objet tag
			card_to_format = NdefFormatable.get(tag);
			card_to_format.connect();

			String data = "formatage";

			// create the message in according with the standard
			String lang = "en";
			byte[] textBytes = data.getBytes();
			byte[] langBytes = lang.getBytes("US-ASCII");
			int langLength = langBytes.length;
			int textLength = textBytes.length;

			byte[] payload = new byte[1 + langLength + textLength];
			payload[0] = (byte) langLength;

			// copy langbytes and textbytes into payload
			System.arraycopy(langBytes, 0, payload, 1, langLength);
			System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

			NdefRecord records = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
					NdefRecord.RTD_TEXT, new byte[0], payload);

			NdefMessage message = new NdefMessage(records);

			card_to_format.format(message);

			tv1.setText("La carte a été formatée avec succès !");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FormatException e) {
			e.printStackTrace();
		}
	}
	
	public void nfc_init()
	{
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // on vérifie si le hardware NFC est présent sur le device
        if (mNfcAdapter == null) {
        	// si le device n'a pas de module NFC, on quitte
            Toast.makeText(this, "This device doesn't support NFC !", Toast.LENGTH_LONG).show();
            tv1.setText("Le NFC n'est pas disponible sur cet appareil.");
            finish();
            return;
        }
        // on vérifié si le NFC est activé
		if (!mNfcAdapter.isEnabled()) {
			// si le NFC n'est pas activé on indique qu'il faut le faire
			Toast.makeText(this, "NFC has to be enabled !", Toast.LENGTH_LONG).show();
			startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
		} else {
			// tout est OK, on attend la suite
			//Toast.makeText(this, "NFC enabled and waiting for a tag to read !", Toast.LENGTH_LONG).show();
		}
	}
}