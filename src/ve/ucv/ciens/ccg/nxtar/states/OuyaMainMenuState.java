/*
 * Copyright (C) 2014 Miguel Angel Astor Romero
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ve.ucv.ciens.ccg.nxtar.states;

import ve.ucv.ciens.ccg.nxtar.NxtARCore;
import ve.ucv.ciens.ccg.nxtar.NxtARCore.game_states_t;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.mappings.Ouya;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class OuyaMainMenuState extends MainMenuStateBase{
	private static final String CLASS_NAME = OuyaMainMenuState.class.getSimpleName();

	private Texture ouyaOButtonTexture;
	private Sprite ouyaOButton;
	private boolean oButtonPressed;
	private int oButtonSelection;

	public OuyaMainMenuState(final NxtARCore core){
		super();

		this.core = core;

		startButton.setPosition(-(startButton.getWidth() / 2), -(startButton.getHeight() / 2));
		startButtonBBox.setPosition(startButton.getX(), startButton.getY());

		calibrationButton.setPosition(-(calibrationButton.getWidth() / 2), (startButton.getY() + startButton.getHeight()) + 10);
		calibrationButtonBBox.setPosition(calibrationButton.getX(), calibrationButton.getY());

		float ledYPos = (-(Gdx.graphics.getHeight() / 2) * 0.5f) + (calibrationButton.getY() * 0.5f);
		clientConnectedLedOn.setSize(clientConnectedLedOn.getWidth() * 0.5f, clientConnectedLedOn.getHeight() * 0.5f);
		clientConnectedLedOn.setPosition(-(clientConnectedLedOn.getWidth() / 2), ledYPos);

		clientConnectedLedOff.setSize(clientConnectedLedOff.getWidth() * 0.5f, clientConnectedLedOff.getHeight() * 0.5f);
		clientConnectedLedOff.setPosition(-(clientConnectedLedOff.getWidth() / 2), ledYPos);

		// TODO: Set calibration led attributes.

		ouyaOButtonTexture = new Texture("data/gfx/gui/OUYA_O.png");
		TextureRegion region = new TextureRegion(ouyaOButtonTexture, ouyaOButtonTexture.getWidth(), ouyaOButtonTexture.getHeight());
		ouyaOButton = new Sprite(region);
		ouyaOButton.setSize(ouyaOButton.getWidth() * 0.6f, ouyaOButton.getHeight() * 0.6f);

		oButtonSelection = 0;
		oButtonPressed = false;
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		core.batch.setProjectionMatrix(pixelPerfectCamera.combined);
		core.batch.begin();{
			core.batch.disableBlending();
			drawBackground(core.batch);
			core.batch.enableBlending();

			if(clientConnected){
				clientConnectedLedOn.draw(core.batch);
			}else{
				clientConnectedLedOff.draw(core.batch);
			}

			// TODO: Render calibration leds.

			startButton.draw(core.batch, 1.0f);
			calibrationButton.draw(core.batch, 1.0f);

			if(oButtonSelection == 0){
				ouyaOButton.setPosition(startButton.getX() - ouyaOButton.getWidth() - 20, startButton.getY() + (ouyaOButton.getHeight() / 2));
			}else if(oButtonSelection == 1){
				ouyaOButton.setPosition(calibrationButton.getX() - ouyaOButton.getWidth() - 20, calibrationButton.getY() + (ouyaOButton.getHeight() / 2));
			}
			ouyaOButton.draw(core.batch);

		}core.batch.end();
	}

	@Override
	public void dispose(){
		super.dispose();
		ouyaOButtonTexture.dispose();
	}

	/*;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
	  ; BEGIN CONTROLLER LISTENER METHODS ;
	  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;*/

	@Override
	public boolean buttonDown(Controller controller, int buttonCode){
		// TODO: Test this.

		if(stateActive){
			if(buttonCode == Ouya.BUTTON_O){
				Gdx.app.log(TAG, CLASS_NAME + ".buttonDown(): O button pressed.");

				if(oButtonSelection == 0){
					if(!clientConnected){
						core.toast("Can't start the game. No client is connected.", true);
					}else{
						oButtonPressed = true;
						startButton.setChecked(true);
					}
				}else if(oButtonSelection == 1){
					if(!clientConnected){
						core.toast("Can't calibrate the camera. No client is connected.", true);
					}else{
						oButtonPressed = true;
						calibrationButton.setChecked(true);
					}
				}
			}else if(buttonCode == Ouya.BUTTON_DPAD_UP){
				Gdx.app.log(TAG, CLASS_NAME + ".buttonDown(): Dpad up button pressed.");
				oButtonSelection = oButtonSelection - 1 < 0 ? NUM_MENU_BUTTONS - 1 : oButtonSelection - 1;
			}else if(buttonCode == Ouya.BUTTON_DPAD_DOWN){
				Gdx.app.log(TAG, CLASS_NAME + ".buttonDown(): Dpad down button pressed.");
				oButtonSelection = (oButtonSelection + 1) % NUM_MENU_BUTTONS;
			}

			return true;
		}else{
			return false;
		}
	}

	@Override
	public boolean buttonUp(Controller controller, int buttonCode){
		// TODO: Test this.

		if(stateActive){
			if(buttonCode == Ouya.BUTTON_O){
				Gdx.app.log(TAG, CLASS_NAME + ".buttonDown(): O button released.");

				if(oButtonPressed){
					oButtonPressed = false;

					if(oButtonSelection == 0){
						startButton.setChecked(false);
						core.nextState = game_states_t.IN_GAME;
					}else if(oButtonSelection == 1){
						calibrationButton.setChecked(false);
						core.nextState = game_states_t.IN_GAME;
					}
				}
			}

			return true;
		}else{
			return false;
		}
	}
}
