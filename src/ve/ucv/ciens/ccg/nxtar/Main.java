package ve.ucv.ciens.ccg.nxtar;

import ve.ucv.ciens.ccg.nxtar.interfaces.MulticastEnabler;
import ve.ucv.ciens.ccg.nxtar.interfaces.NetworkConnectionListener;
import ve.ucv.ciens.ccg.nxtar.interfaces.Toaster;
import ve.ucv.ciens.ccg.nxtar.network.RobotControlThread;
import ve.ucv.ciens.ccg.nxtar.network.ServiceDiscoveryThread;
import ve.ucv.ciens.ccg.nxtar.network.VideoStreamingThread;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Main implements ApplicationListener, NetworkConnectionListener {
	private static final String TAG = "NXTAR_CORE_MAIN";
	private static final String CLASS_NAME = Main.class.getSimpleName();
	
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Texture texture;
	private Sprite sprite;
	private Toaster toaster;
	private MulticastEnabler mcastEnabler;
	private int connections;

	private ServiceDiscoveryThread udpThread;
	private VideoStreamingThread videoThread;
	private RobotControlThread robotThread;

	public Main(Toaster toaster, MulticastEnabler mcastEnabler){
		super();
		this.toaster = toaster;
		this.mcastEnabler = mcastEnabler;
		connections = 0;
	}

	@Override
	public void create() {		
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		Gdx.app.setLogLevel(Application.LOG_DEBUG);

		camera = new OrthographicCamera(1, h/w);
		batch = new SpriteBatch();

		texture = new Texture(Gdx.files.internal("data/libgdx.png"));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		TextureRegion region = new TextureRegion(texture, 0, 0, 512, 275);

		sprite = new Sprite(region);
		sprite.setSize(0.9f, 0.9f * sprite.getHeight() / sprite.getWidth());
		sprite.setOrigin(sprite.getWidth()/2, sprite.getHeight()/2);
		sprite.setPosition(-sprite.getWidth()/2, -sprite.getHeight()/2);

		Gdx.app.debug(TAG, CLASS_NAME + ".create() :: Creating network threads");
		mcastEnabler.enableMulticast();
		udpThread = new ServiceDiscoveryThread();
		videoThread = new VideoStreamingThread(toaster);
		robotThread = new RobotControlThread(toaster);

		udpThread.start();
		videoThread.start();
		robotThread.start();
	}

	@Override
	public void dispose() {
		batch.dispose();
		texture.dispose();
	}

	@Override
	public void render() {		
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		sprite.draw(batch);
		batch.end();
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public synchronized void interfaceConnected(String iface) {
		if(iface.compareTo(VideoStreamingThread.THREAD_NAME) == 0 || iface.compareTo(RobotControlThread.THREAD_NAME) == 0)
			connections += 1;
		if(connections >= 2){
			Gdx.app.debug(TAG, CLASS_NAME + ".interfaceConnected() :: Stopping service broadcast.");
			udpThread.finish();
			mcastEnabler.disableMulticast();
		}
	}
}
