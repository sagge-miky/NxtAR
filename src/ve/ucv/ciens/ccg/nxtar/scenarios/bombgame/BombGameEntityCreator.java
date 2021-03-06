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
package ve.ucv.ciens.ccg.nxtar.scenarios.bombgame;

import java.util.LinkedList;
import java.util.List;

import ve.ucv.ciens.ccg.nxtar.components.AnimationComponent;
import ve.ucv.ciens.ccg.nxtar.components.AutomaticMovementComponent;
import ve.ucv.ciens.ccg.nxtar.components.CollisionDetectionComponent;
import ve.ucv.ciens.ccg.nxtar.components.CollisionModelComponent;
import ve.ucv.ciens.ccg.nxtar.components.EnvironmentComponent;
import ve.ucv.ciens.ccg.nxtar.components.GeometryComponent;
import ve.ucv.ciens.ccg.nxtar.components.MarkerCodeComponent;
import ve.ucv.ciens.ccg.nxtar.components.PlayerComponentBase;
import ve.ucv.ciens.ccg.nxtar.components.RenderModelComponent;
import ve.ucv.ciens.ccg.nxtar.components.ShaderComponent;
import ve.ucv.ciens.ccg.nxtar.components.VisibilityComponent;
import ve.ucv.ciens.ccg.nxtar.entities.EntityCreatorBase;
import ve.ucv.ciens.ccg.nxtar.graphics.shaders.DirectionalLightPerPixelShader;
import ve.ucv.ciens.ccg.nxtar.scenarios.bombgame.BombComponent.bomb_type_t;
import ve.ucv.ciens.ccg.nxtar.systems.AnimationSystem;
import ve.ucv.ciens.ccg.nxtar.systems.CollisionDetectionSystem;

import com.artemis.Entity;
import com.artemis.managers.GroupManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class BombGameEntityCreator extends EntityCreatorBase{
	private static final String  TAG                                         = "BOMB_ENTITY_CREATOR";
	private static final String  CLASS_NAME                                  = BombGameEntityCreator.class.getSimpleName();
	private static final boolean DEBUG_RENDER_BOMB_COLLISION_MODELS          = false;
	private static final boolean DEBUG_RENDER_DOOR_COLLISION_MODELS          = false;
	private static final boolean DEBUG_RENDER_PARAPHERNALIA_COLLISION_MODELS = false;
	public static  final String  DOORS_GROUP                                 = "DOORS";
	public static  final Vector3 ROBOT_ARM_START_POINT                       = new Vector3(0.0f, 0.0f, -1.0f);
	public static  final int     DOOR_OPEN_ANIMATION                         = 1;
	public static  final int     DOOR_CLOSE_ANIMATION                        = 0;
	public static        int     NUM_BOMBS                                   = 0;

	private class EntityParameters{
		public Environment environment;
		public Shader      shader;
		public int         markerCode;
		public int         nextAnimation;
		public boolean     loopAnimation;

		public EntityParameters(){
			environment   = new Environment();
			shader        = null;
			markerCode    = -1;
			nextAnimation = -1;
			loopAnimation = false;
		}
	}

	private Shader       shader;
	private int          currentBombId;
	private GroupManager groupManager;
	private List<Entity> entities;
	private Entity       player;

	// Render models.
	private Model  robotArmModel                       = null;
	private Model  doorModel                           = null;
	private Model  doorFrameModel                      = null;
	private Model  combinationBombModel                = null;
	private Model  combinationButton1Model             = null;
	private Model  combinationButton2Model             = null;
	private Model  combinationButton3Model             = null;
	private Model  combinationButton4Model             = null;
	private Model  inclinationBombModel                = null;
	private Model  inclinationBombButtonModel          = null;
	private Model  wiresBombModel                      = null;
	private Model  wiresBombModelWire1                 = null;
	private Model  wiresBombModelWire2                 = null;
	private Model  wiresBombModelWire3                 = null;
	private Model  monkeyModel                         = null;

	// Collision models.
	private Model  robotArmCollisionModel              = null;
	private Model  doorCollisionModel                  = null;
	private Model  doorFrameCollisionModel             = null;
	private Model  combinationBombCollisionModel       = null;
	private Model  combinationButton1CollisionModel    = null;
	private Model  combinationButton2CollisionModel    = null;
	private Model  combinationButton3CollisionModel    = null;
	private Model  combinationButton4CollisionModel    = null;
	private Model  inclinationBombCollisionModel       = null;
	private Model  inclinationBombButtonCollisionModel = null;
	private Model  wiresBombCollisionModel             = null;
	private Model  wiresBombCollisionModelWire1        = null;
	private Model  wiresBombCollisionModelWire2        = null;
	private Model  wiresBombCollisionModelWire3        = null;

	public BombGameEntityCreator(){
		currentBombId = 0;
		manager = new AssetManager();
		entities = new LinkedList<Entity>();
		player = null;

		// Load the shader.
		shader = new DirectionalLightPerPixelShader();
		try{
			shader.init();
		}catch(GdxRuntimeException gdx){
			Gdx.app.error(TAG, CLASS_NAME + ".BombGameEntityCreator(): Shader failed to load: " + gdx.getMessage());
			shader = null;
		}

		// Load the render models.
		manager.load("models/render_models/bomb_game/robot_arm.g3db", Model.class);
		manager.load("models/render_models/bomb_game/door.g3db", Model.class);
		manager.load("models/render_models/bomb_game/door_frame1.g3db", Model.class);

		manager.load("models/render_models/bomb_game/bomb_3_body.g3db", Model.class);
		manager.load("models/render_models/bomb_game/bomb_3_btn_1.g3db", Model.class);
		manager.load("models/render_models/bomb_game/bomb_3_btn_2.g3db", Model.class);
		manager.load("models/render_models/bomb_game/bomb_3_btn_3.g3db", Model.class);
		manager.load("models/render_models/bomb_game/bomb_3_btn_4.g3db", Model.class);

		manager.load("models/render_models/bomb_game/bomb_2_body.g3db", Model.class);
		manager.load("models/render_models/bomb_game/big_btn.g3db", Model.class);

		manager.load("models/render_models/bomb_game/bomb_1_body.g3db", Model.class);
		manager.load("models/render_models/bomb_game/cable_1.g3db", Model.class);
		manager.load("models/render_models/bomb_game/cable_2.g3db", Model.class);
		manager.load("models/render_models/bomb_game/cable_3.g3db", Model.class);
		manager.load("models/render_models/bomb_game/monkey.g3db", Model.class);

		// Load the collision models.
		manager.load("models/collision_models/bomb_game/robot_arm_col.g3db", Model.class);
		manager.load("models/collision_models/bomb_game/door_col.g3db", Model.class);
		manager.load("models/collision_models/bomb_game/door_frame1_col.g3db", Model.class);

		manager.load("models/collision_models/bomb_game/bomb_3_body_col.g3db", Model.class);
		manager.load("models/collision_models/bomb_game/bomb_3_btn_1_col.g3db", Model.class);
		manager.load("models/collision_models/bomb_game/bomb_3_btn_2_col.g3db", Model.class);
		manager.load("models/collision_models/bomb_game/bomb_3_btn_3_col.g3db", Model.class);
		manager.load("models/collision_models/bomb_game/bomb_3_btn_4_col.g3db", Model.class);

		manager.load("models/collision_models/bomb_game/bomb_2_body_col.g3db", Model.class);
		manager.load("models/collision_models/bomb_game/big_btn_col.g3db", Model.class);

		manager.load("models/collision_models/bomb_game/bomb_1_body_col.g3db", Model.class);
		manager.load("models/collision_models/bomb_game/cable_1_col.g3db", Model.class);
		manager.load("models/collision_models/bomb_game/cable_2_col.g3db", Model.class);
		manager.load("models/collision_models/bomb_game/cable_3_col.g3db", Model.class);
	}

	@Override
	public void createAllEntities(){
		EntityParameters parameters;
		Entity monkey;

		groupManager = world.getManager(GroupManager.class);

		// Create and set the lighting.
		parameters = new EntityParameters();
		parameters.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.3f, 0.3f, 0.3f, 1.0f));
		parameters.environment.add(new DirectionalLight().set(new Color(1, 1, 1, 1), new Vector3(0, 0, -1)));
		parameters.shader = shader;

		addRobotArm(parameters);

		// Add bombs.
		parameters.markerCode = 89;
		addBomb(parameters, bomb_type_t.COMBINATION);
		parameters.markerCode = 90;
		addBomb(parameters, bomb_type_t.INCLINATION);
		parameters.markerCode = 91;
		addBomb(parameters, bomb_type_t.WIRES);

		// Add doors.
		parameters.nextAnimation = AnimationSystem.NO_ANIMATION;
		parameters.loopAnimation = false;

		parameters.markerCode = 89;
		addDoor(parameters);
		parameters.markerCode = 90;
		addDoor(parameters);
		parameters.markerCode = 91;
		addDoor(parameters);

		// Add the monkey.
		monkey = world.createEntity();
		monkey.addComponent(new RenderModelComponent(monkeyModel));
		monkey.addComponent(new GeometryComponent());
		monkey.addComponent(new MarkerCodeComponent(1023));
		monkey.addComponent(new VisibilityComponent());
		monkey.addComponent(new ShaderComponent(shader));
		monkey.addComponent(new EnvironmentComponent(parameters.environment));
		monkey.addToWorld();
		entities.add(monkey);

		// Create the player.
		if(player == null){
			player = world.createEntity();
			player.addComponent(new BombGamePlayerComponent(3));
			groupManager.add(player, PlayerComponentBase.PLAYER_GROUP);
			player.addToWorld();
		}else{
			player.getComponent(BombGamePlayerComponent.class).reset();
		}

		entitiesCreated = true;
	}

	@Override
	public boolean updateAssetManager() throws NullPointerException{
		boolean doneLoading;

		if(core == null)
			throw new NullPointerException("Core has not been set.");

		doneLoading = manager.update();
		if(doneLoading){
			getModels();
			createAllEntities();
			core.onAssetsLoaded();
		}

		return doneLoading;
	}

	@Override
	public void dispose() {
		if(shader != null) shader.dispose();
		manager.dispose();
	}

	private void addRobotArm(EntityParameters parameters){
		Entity robotArm = world.createEntity();

		robotArm.addComponent(new GeometryComponent(new Vector3(ROBOT_ARM_START_POINT), new Matrix3(), new Vector3(1, 1, 1)));
		robotArm.addComponent(new EnvironmentComponent(parameters.environment));
		robotArm.addComponent(new ShaderComponent(parameters.shader));
		robotArm.addComponent(new RenderModelComponent(robotArmModel));
		robotArm.addComponent(new CollisionModelComponent(robotArmCollisionModel));
		robotArm.addComponent(new CollisionDetectionComponent());
		robotArm.addComponent(new AutomaticMovementComponent());
		robotArm.addToWorld();
		entities.add(robotArm);
	}

	private void addBomb(EntityParameters parameters, bomb_type_t type) throws IllegalArgumentException{
		Entity bomb;

		// Create a bomb entity and add it's generic components.
		bomb = world.createEntity();
		bomb.addComponent(new GeometryComponent(new Vector3(), new Matrix3(), new Vector3(1, 1, 1)));
		bomb.addComponent(new EnvironmentComponent(parameters.environment));
		bomb.addComponent(new ShaderComponent(parameters.shader));
		bomb.addComponent(new MarkerCodeComponent(parameters.markerCode));
		bomb.addComponent(new BombComponent(currentBombId, type));
		bomb.addComponent(new VisibilityComponent());

		// Add the collision and render models depending on the bomb type.
		if(type == bomb_type_t.COMBINATION){
			bomb.addComponent(new RenderModelComponent(combinationBombModel));
			bomb.addComponent(new CollisionModelComponent(combinationBombCollisionModel));
			addBombCombinationButtons(parameters);
			if(DEBUG_RENDER_BOMB_COLLISION_MODELS)
				addDebugCollisionModelRenderingEntity(combinationBombCollisionModel, parameters, false);

		}else if(type == bomb_type_t.INCLINATION){
			bomb.addComponent(new RenderModelComponent(inclinationBombModel));
			bomb.addComponent(new CollisionModelComponent(inclinationBombCollisionModel));
			addBombInclinationButton(parameters);
			if(DEBUG_RENDER_BOMB_COLLISION_MODELS)
				addDebugCollisionModelRenderingEntity(inclinationBombCollisionModel, parameters, false);

		}else if(type == bomb_type_t.WIRES){
			bomb.addComponent(new RenderModelComponent(wiresBombModel));
			bomb.addComponent(new CollisionModelComponent(wiresBombCollisionModel));
			addBombWires(parameters);
			if(DEBUG_RENDER_BOMB_COLLISION_MODELS)
				addDebugCollisionModelRenderingEntity(wiresBombCollisionModel, parameters, false);

		}else
			throw new IllegalArgumentException("Unrecognized bomb type: " + Integer.toString(type.getValue()));

		// Add the bomb to the world and the respective marker group. Then increase the id for the next bomb.
		groupManager.add(bomb, Integer.toString(parameters.markerCode));
		bomb.addToWorld();
		entities.add(bomb);
		currentBombId++;
		NUM_BOMBS++;
	}

	private void addBombCombinationButtons(EntityParameters parameters){
		Entity button1, button2, button3, button4;

		button1 = addBombParaphernalia(combinationButton1Model, combinationButton1CollisionModel, parameters);
		button2 = addBombParaphernalia(combinationButton2Model, combinationButton2CollisionModel, parameters);
		button3 = addBombParaphernalia(combinationButton3Model, combinationButton3CollisionModel, parameters);
		button4 = addBombParaphernalia(combinationButton4Model, combinationButton4CollisionModel, parameters);

		button1.addComponent(new BombGameEntityTypeComponent(BombGameEntityTypeComponent.COM_BUTTON_1));
		button2.addComponent(new BombGameEntityTypeComponent(BombGameEntityTypeComponent.COM_BUTTON_2));
		button3.addComponent(new BombGameEntityTypeComponent(BombGameEntityTypeComponent.COM_BUTTON_3));
		button4.addComponent(new BombGameEntityTypeComponent(BombGameEntityTypeComponent.COM_BUTTON_4));

		button1.addToWorld();
		button2.addToWorld();
		button3.addToWorld();
		button4.addToWorld();
	}

	private void addBombInclinationButton(EntityParameters parameters){
		Entity button;

		button = addBombParaphernalia(inclinationBombButtonModel, inclinationBombButtonCollisionModel, parameters);
		button.addComponent(new BombGameEntityTypeComponent(BombGameEntityTypeComponent.BIG_BUTTON));
		button.addToWorld();
	}

	private void addBombWires(EntityParameters parameters){
		Entity wire1, wire2, wire3;

		wire1 = addBombParaphernalia(wiresBombModelWire1, wiresBombCollisionModelWire1, parameters);
		wire2 = addBombParaphernalia(wiresBombModelWire2, wiresBombCollisionModelWire2, parameters);
		wire3 = addBombParaphernalia(wiresBombModelWire3, wiresBombCollisionModelWire3, parameters);

		wire1.addComponent(new BombGameEntityTypeComponent(BombGameEntityTypeComponent.BOMB_WIRE_1));
		wire2.addComponent(new BombGameEntityTypeComponent(BombGameEntityTypeComponent.BOMB_WIRE_2));
		wire3.addComponent(new BombGameEntityTypeComponent(BombGameEntityTypeComponent.BOMB_WIRE_3));

		wire1.addToWorld();
		wire2.addToWorld();
		wire3.addToWorld();
	}

	private Entity addBombParaphernalia(Model renderModel, Model collisionModel, EntityParameters parameters){
		Entity thing;

		thing = world.createEntity();
		thing.addComponent(new GeometryComponent(new Vector3(), new Matrix3(), new Vector3(1, 1, 1)));
		thing.addComponent(new EnvironmentComponent(parameters.environment));
		thing.addComponent(new ShaderComponent(parameters.shader));
		thing.addComponent(new RenderModelComponent(renderModel));
		thing.addComponent(new CollisionModelComponent(collisionModel));
		thing.addComponent(new VisibilityComponent());
		thing.addComponent(new MarkerCodeComponent(parameters.markerCode));
		thing.addComponent(new CollisionDetectionComponent());
		groupManager.add(thing, CollisionDetectionSystem.COLLIDABLE_OBJECTS_GROUP);
		groupManager.add(thing, Integer.toString(parameters.markerCode));

		if(DEBUG_RENDER_PARAPHERNALIA_COLLISION_MODELS)
			addDebugCollisionModelRenderingEntity(collisionModel, parameters, false);

		entities.add(thing);

		return thing;
	}

	private void addDoor(EntityParameters parameters){
		ModelInstance doorInstance, doorColInstance;
		Entity frame, door;

		frame = world.createEntity();
		frame.addComponent(new GeometryComponent(new Vector3(), new Matrix3(), new Vector3(1, 1, 1)));
		frame.addComponent(new RenderModelComponent(doorFrameModel));
		frame.addComponent(new CollisionModelComponent(doorFrameCollisionModel));
		frame.addComponent(new CollisionDetectionComponent());
		frame.addComponent(new EnvironmentComponent(parameters.environment));
		frame.addComponent(new ShaderComponent(parameters.shader));
		frame.addComponent(new VisibilityComponent());
		frame.addComponent(new MarkerCodeComponent(parameters.markerCode));
		frame.addComponent(new BombGameEntityTypeComponent(BombGameEntityTypeComponent.DOOR_FRAME));
		groupManager.add(frame, Integer.toString(parameters.markerCode));
		frame.addToWorld();

		door = world.createEntity();
		door.addComponent(new GeometryComponent(new Vector3(), new Matrix3(), new Vector3(1, 1, 1)));
		door.addComponent(new RenderModelComponent(doorModel));
		door.addComponent(new CollisionModelComponent(doorCollisionModel));
		door.addComponent(new EnvironmentComponent(parameters.environment));
		door.addComponent(new ShaderComponent(parameters.shader));
		door.addComponent(new MarkerCodeComponent(parameters.markerCode));
		door.addComponent(new VisibilityComponent());
		doorInstance    = door.getComponent(RenderModelComponent.class).instance;
		doorColInstance = door.getComponent(CollisionModelComponent.class).instance;
		door.addComponent(new AnimationComponent(doorInstance, parameters.nextAnimation, parameters.loopAnimation, doorColInstance));
		door.addComponent(new CollisionDetectionComponent());
		door.addComponent(new BombGameEntityTypeComponent(BombGameEntityTypeComponent.DOOR));
		groupManager.add(door, CollisionDetectionSystem.COLLIDABLE_OBJECTS_GROUP);
		groupManager.add(door, Integer.toString(parameters.markerCode));
		groupManager.add(door, DOORS_GROUP);
		door.addToWorld();

		entities.add(frame);
		entities.add(door);

		if(DEBUG_RENDER_DOOR_COLLISION_MODELS){
			addDebugCollisionModelRenderingEntity(doorFrameCollisionModel, parameters, false);
			addDebugCollisionModelRenderingEntity(doorCollisionModel, parameters, true);
		}
	}

	private void addDebugCollisionModelRenderingEntity(Model collisionModel, EntityParameters parameters, boolean animation){
		ModelInstance instance;
		Entity thing;

		thing = world.createEntity();
		thing.addComponent(new GeometryComponent(new Vector3(), new Matrix3(), new Vector3(1, 1, 1)));
		thing.addComponent(new EnvironmentComponent(parameters.environment));
		thing.addComponent(new ShaderComponent(parameters.shader));
		thing.addComponent(new RenderModelComponent(collisionModel));
		thing.addComponent(new VisibilityComponent());
		thing.addComponent(new MarkerCodeComponent(parameters.markerCode));
		if(animation){
			instance = thing.getComponent(RenderModelComponent.class).instance;
			thing.addComponent(new AnimationComponent(instance, parameters.nextAnimation, parameters.loopAnimation));
		}
		thing.addToWorld();
		entities.add(thing);
	}

	private void getModels(){
		// Get the render models.
		robotArmModel              = manager.get("models/render_models/bomb_game/robot_arm.g3db", Model.class);
		doorModel                  = manager.get("models/render_models/bomb_game/door.g3db", Model.class);
		doorFrameModel             = manager.get("models/render_models/bomb_game/door_frame1.g3db", Model.class);

		combinationBombModel       = manager.get("models/render_models/bomb_game/bomb_3_body.g3db", Model.class);
		combinationButton1Model    = manager.get("models/render_models/bomb_game/bomb_3_btn_1.g3db", Model.class);
		combinationButton2Model    = manager.get("models/render_models/bomb_game/bomb_3_btn_2.g3db", Model.class);
		combinationButton3Model    = manager.get("models/render_models/bomb_game/bomb_3_btn_3.g3db", Model.class);
		combinationButton4Model    = manager.get("models/render_models/bomb_game/bomb_3_btn_4.g3db", Model.class);

		inclinationBombModel       = manager.get("models/render_models/bomb_game/bomb_2_body.g3db", Model.class);
		inclinationBombButtonModel = manager.get("models/render_models/bomb_game/big_btn.g3db", Model.class);

		wiresBombModel             = manager.get("models/render_models/bomb_game/bomb_1_body.g3db", Model.class);
		wiresBombModelWire1        = manager.get("models/render_models/bomb_game/cable_1.g3db", Model.class);
		wiresBombModelWire2        = manager.get("models/render_models/bomb_game/cable_2.g3db", Model.class);
		wiresBombModelWire3        = manager.get("models/render_models/bomb_game/cable_3.g3db", Model.class);
		monkeyModel                = manager.get("models/render_models/bomb_game/monkey.g3db", Model.class);

		// Get the collision models.
		robotArmCollisionModel              = manager.get("models/collision_models/bomb_game/robot_arm_col.g3db", Model.class);
		doorCollisionModel                  = manager.get("models/collision_models/bomb_game/door_col.g3db", Model.class);
		doorFrameCollisionModel             = manager.get("models/collision_models/bomb_game/door_frame1_col.g3db", Model.class);

		combinationBombCollisionModel       = manager.get("models/collision_models/bomb_game/bomb_3_body_col.g3db", Model.class);
		combinationButton1CollisionModel    = manager.get("models/collision_models/bomb_game/bomb_3_btn_1_col.g3db", Model.class);
		combinationButton2CollisionModel    = manager.get("models/collision_models/bomb_game/bomb_3_btn_2_col.g3db", Model.class);
		combinationButton3CollisionModel    = manager.get("models/collision_models/bomb_game/bomb_3_btn_3_col.g3db", Model.class);
		combinationButton4CollisionModel    = manager.get("models/collision_models/bomb_game/bomb_3_btn_4_col.g3db", Model.class);

		inclinationBombCollisionModel       = manager.get("models/collision_models/bomb_game/bomb_2_body_col.g3db", Model.class);
		inclinationBombButtonCollisionModel = manager.get("models/collision_models/bomb_game/big_btn_col.g3db", Model.class);

		wiresBombCollisionModel             = manager.get("models/collision_models/bomb_game/bomb_1_body_col.g3db", Model.class);
		wiresBombCollisionModelWire1        = manager.get("models/collision_models/bomb_game/cable_1_col.g3db", Model.class);
		wiresBombCollisionModelWire2        = manager.get("models/collision_models/bomb_game/cable_2_col.g3db", Model.class);
		wiresBombCollisionModelWire3        = manager.get("models/collision_models/bomb_game/cable_3_col.g3db", Model.class);
	}

	@Override
	public void resetAllEntities() {
		for(Entity entity : entities){
			try{
				if(entity.isActive())
					entity.deleteFromWorld();
			}catch(NullPointerException n){
				Gdx.app.error(TAG, CLASS_NAME + ".resetAllEntities(): Null pointer exception while deleting entity.");
			}
		}
		entities.clear();
		NUM_BOMBS = 0;
		createAllEntities();
	}
}
