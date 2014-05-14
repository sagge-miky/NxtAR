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
package ve.ucv.ciens.ccg.nxtar.systems;

import ve.ucv.ciens.ccg.nxtar.components.GeometryComponent;
import ve.ucv.ciens.ccg.nxtar.components.MarkerCodeComponent;
import ve.ucv.ciens.ccg.nxtar.components.MeshComponent;
import ve.ucv.ciens.ccg.nxtar.components.ShaderComponent;
import ve.ucv.ciens.ccg.nxtar.graphics.LightSource;
import ve.ucv.ciens.ccg.nxtar.graphics.RenderParameters;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

/**
 * <p>Entity processing system in charge of rendering 3D objects using OpenGL. The
 * entities to be rendered must have a geometry, shader and mesh component associated.</p>
 */
public class ObjectRenderingSystem extends EntityProcessingSystem {
	@Mapper ComponentMapper<GeometryComponent> geometryMapper;
	@Mapper ComponentMapper<ShaderComponent> shaderMapper;
	@Mapper ComponentMapper<MeshComponent> modelMapper;

	private static final Vector3 LIGHT_POSITION = new Vector3(2.0f, 2.0f, 4.0f);
	private static final Color AMBIENT_COLOR = new Color(0.0f, 0.1f, 0.2f, 1.0f);
	private static final Color DIFFUSE_COLOR = new Color(1.0f, 1.0f, 1.0f, 1.0f);
	private static final Color SPECULAR_COLOR = new Color(1.0f, 0.8f, 0.0f, 1.0f);
	private static final float SHINYNESS = 50.0f;

	/**
	 * <p>A matrix representing 3D translations.</p>
	 */
	private Matrix4 translationMatrix;

	/**
	 * <p>A matrix representing 3D rotations.</p>
	 */
	private Matrix4 rotationMatrix;

	/**
	 * <p>A matrix representing 3D scalings.</p>
	 */
	private Matrix4 scalingMatrix;

	/**
	 * <p>The total transformation to be applied to an entity.</p>
	 */
	private Matrix4 combinedTransformationMatrix;

	@SuppressWarnings("unchecked")
	public ObjectRenderingSystem() {
		super(Aspect.getAspectForAll(GeometryComponent.class, ShaderComponent.class, MeshComponent.class).exclude(MarkerCodeComponent.class));

		RenderParameters.setLightSource1(new LightSource(LIGHT_POSITION, AMBIENT_COLOR, DIFFUSE_COLOR, SPECULAR_COLOR, SHINYNESS));

		translationMatrix = new Matrix4().setToTranslation(0.0f, 0.0f, 0.0f);
		rotationMatrix = new Matrix4().idt();
		scalingMatrix = new Matrix4().setToScaling(0.0f, 0.0f, 0.0f);
		combinedTransformationMatrix = new Matrix4();
	}

	/**
	 * <p>Renders the entity passed by parameter, calculating it's corresponding geometric
	 * transformation and setting and calling it's associated shader program.</p>
	 * 
	 * @param e The entity to be processed.
	 */
	@Override
	protected void process(Entity e) {
		GeometryComponent geometryComponent;
		ShaderComponent shaderComponent;
		MeshComponent meshComponent;

		// Get the necessary components.
		geometryComponent = geometryMapper.get(e);
		meshComponent = modelMapper.get(e);
		shaderComponent = shaderMapper.get(e);

		// Calculate the geometric transformation for this entity.
		translationMatrix.setToTranslation(geometryComponent.position);
		rotationMatrix.set(geometryComponent.rotation);
		scalingMatrix.setToScaling(geometryComponent.scaling);
		combinedTransformationMatrix.idt().mul(translationMatrix).mul(rotationMatrix).mul(scalingMatrix);

		// Set up the global rendering parameters for this frame.
		RenderParameters.setTransformationMatrix(combinedTransformationMatrix);

		// Render this entity.
		shaderComponent.shader.getShaderProgram().begin();{
			shaderComponent.shader.setUniforms();
			meshComponent.model.render(shaderComponent.shader.getShaderProgram(), GL20.GL_TRIANGLES);
		}shaderComponent.shader.getShaderProgram().end();
	}
}
