package to.rent.rentto.Login;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import to.rent.rentto.R;

public class VideoActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final String LOG_TAG = "VideoActivity";
    SurfaceView videoView;
    MediaPlayer player;
    private SurfaceHolder holder;
    private boolean hasActiveHolder;
    private Button button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        videoView = (SurfaceView) findViewById(R.id.videoView);
        button = (Button) findViewById(R.id.signUpButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VideoActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        button.setVisibility(View.GONE);
        final String videoPath = "android.resource://to.rent.rentto/"+R.raw.video;
        player = new MediaPlayer();
        holder = videoView.getHolder();
        holder.addCallback(this);
//        holder = videoView.getHolder();
//        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//        player.setDisplay(holder);
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                // Adjust the size of the video
                // so it fits on the screen
                int videoWidth = player.getVideoWidth();
                int videoHeight = player.getVideoHeight();
                float videoProportion = (float) videoWidth / (float) videoHeight;
                int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
                int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
                float screenProportion = (float) screenWidth / (float) screenHeight;
                android.view.ViewGroup.LayoutParams lp = videoView.getLayoutParams();

                if (videoProportion > screenProportion) {
                    lp.width = screenWidth;
                    lp.height = (int) ((float) screenWidth / videoProportion);
                } else {
                    lp.width = (int) (videoProportion * (float) screenHeight);
                    lp.height = screenHeight;
                }
                videoView.setLayoutParams(lp);
                if (!player.isPlaying()) {
                    player.start();
                }

            }
        });
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                button.setVisibility(View.VISIBLE);
            }
        });
    }

    private void playVideo(){
        final String videoPath = "android.resource://to.rent.rentto/"+R.raw.video2;
        new Thread(new Runnable() {
            public void run() {
                try {
                    player.setDataSource(VideoActivity.this, Uri.parse(videoPath));
                    player.prepare();
                } catch (Exception e) { // I can split the exceptions to get which error i need.
                    //Toast.makeText(VideoActivity.this, "Error watching video", Toast.LENGTH_LONG).show();
                    Log.i(LOG_TAG, "Error");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.stop();
    }

    @Override
    public void onPause() {
        super.onPause();
        player.pause();
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        final Surface surface = holder.getSurface();

        if ( surface == null ) return;

        // on pre Ice Scream Sandwich (4.0) versions invalid surfaces seems to be accepted (or at least do not cause crash)
//        final boolean invalidSurfaceAccepted = Build.VERSION.SDK_INT < Build.ICE_CREAM_SANDWICH;
//        final boolean invalidSurface = ! surface.isValid();
//
//        if ( invalidSurface && ( ! invalidSurfaceAccepted ) ) return;

        player.setDisplay(holder);
        playVideo();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}
