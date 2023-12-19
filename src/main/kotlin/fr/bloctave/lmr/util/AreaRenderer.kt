package fr.bloctave.lmr.util

import fr.bloctave.lmr.data.areas.Area
import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import fr.bloctave.lmr.config.ClientConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderState
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.Direction
import net.minecraft.util.Direction.*
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.vector.Matrix4f
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.util.math.vector.Vector3f
import net.minecraft.util.text.StringTextComponent
import org.lwjgl.opengl.GL11
import java.awt.Color

object AreaRenderer {
	private val mc = Minecraft.getInstance()
	private val offsets = mapOf(
		// First vector is the inside offset
		// Second is the outside offset
		// Third is the other inside offset for the internal faces
		UP to listOf(
			createVecTriple(1, 1, -1, -1, 1, 1, 1, -1, -1),    // South West -> -X, +Z
			createVecTriple(-1, 1, -1, 1, 1, 1, -1, -1, -1),   // South East -> +X, +Z
			createVecTriple(-1, 1, 1, 1, 1, -1, -1, -1, 1),    // North East -> +X, -Z
			createVecTriple(1, 1, 1, -1, 1, -1, 1, -1, 1)      // North West -> -X, -Z
		),
		DOWN to listOf(
			createVecTriple(1, -1, 1, -1, -1, -1, 1, 1, 1),    // North West -> -X, -Z
			createVecTriple(-1, -1, 1, 1, -1, -1, -1, 1, 1),   // North East -> +X, -Z
			createVecTriple(-1, -1, -1, 1, -1, 1, -1, 1, -1),  // South East -> +X, +Z
			createVecTriple(1, -1, -1, -1, -1, 1, 1, 1, -1)    // South West -> -X, +Z
		),
		NORTH to listOf(
			createVecTriple(-1, 1, -1, 1, -1, -1, -1, 1, 1),   // Down East -> +X, -Y
			createVecTriple(1, 1, -1, -1, -1, -1, 1, 1, 1),    // Down West -> -X, -Y
			createVecTriple(1, -1, -1, -1, 1, -1, 1, -1, 1),   // Up West -> -X, +Y
			createVecTriple(-1, -1, -1, 1, 1, -1, -1, -1, 1)   // Up East -> +X, +Y
		),
		SOUTH to listOf(
			createVecTriple(1, 1, 1, -1, -1, 1, 1, 1, -1),     // Down West -> -X, -Y
			createVecTriple(-1, 1, 1, 1, -1, 1, -1, 1, -1),    // Down East -> +X, -Y
			createVecTriple(-1, -1, 1, 1, 1, 1, -1, -1, -1),   // Up East -> +X, +Y
			createVecTriple(1, -1, 1, -1, 1, 1, 1, -1, -1)     // Up West -> -X, +Y
		),
		EAST to listOf(
			createVecTriple(1, 1, -1, 1, -1, 1, -1, 1, -1),    // Down South -> +Z, -Y
			createVecTriple(1, 1, 1, 1, -1, -1, -1, 1, 1),     // Down North -> -Z, -Y
			createVecTriple(1, -1, 1, 1, 1, -1, -1, -1, 1),    // Up North -> -Z, +Y
			createVecTriple(1, -1, -1, 1, 1, 1, -1, -1, -1)    // Up South -> +Z, +Y
		),
		WEST to listOf(
			createVecTriple(-1, 1, 1, -1, -1, -1, 1, 1, 1),    // Down North -> -Z, -Y
			createVecTriple(-1, 1, -1, -1, -1, 1, 1, 1, -1),   // Down South -> +Z, -Y
			createVecTriple(-1, -1, -1, -1, 1, 1, 1, -1, -1),  // Up South -> +Z, +Y
			createVecTriple(-1, -1, 1, -1, 1, -1, 1, -1, 1)    // Up North -> -Z, +Y
		)
	)

	private val renderTypeAreaEdge = createRenderType(
		"area_edge",
		GL11.GL_QUAD_STRIP,
		RenderType.State.builder()
			.setLayeringState(RenderState.VIEW_OFFSET_Z_LAYERING)
			.setCullState(RenderState.NO_CULL)
			.setWriteMaskState(RenderState.COLOR_WRITE)
			.createCompositeState(true)
	)
	private val renderTypeAreaSide = createRenderType(
		"area_side",
		GL11.GL_QUADS,
		RenderType.State.builder()
			.setLayeringState(RenderState.VIEW_OFFSET_Z_LAYERING)
			.setTransparencyState(RenderState.TRANSLUCENT_TRANSPARENCY)
			.setCullState(RenderState.NO_CULL)
			.setWriteMaskState(RenderState.COLOR_WRITE)
			.createCompositeState(true)
	)

	private fun createRenderType(name: String, drawMode: Int, state: RenderType.State): RenderType.Type =
		RenderType.create(name, DefaultVertexFormats.POSITION_COLOR, drawMode, 256, state)

	private fun createVecTriple(
		x1: Int, y1: Int, z1: Int,
		x2: Int, y2: Int, z2: Int,
		x3: Int, y3: Int, z3: Int
	): Triple<Vector3f, Vector3f, Vector3f> = Triple(
		Vector3f(x1.toFloat(), y1.toFloat(), z1.toFloat()),
		Vector3f(x2.toFloat(), y2.toFloat(), z2.toFloat()),
		Vector3f(x3.toFloat(), y3.toFloat(), z3.toFloat())
	)

	private fun renderWithType(
		renderType: RenderType.Type,
		matrixStack: MatrixStack,
		render: IVertexBuilder.(Matrix4f) -> Unit
	) {
		val buffer = mc.renderBuffers().bufferSource()
		render(buffer.getBuffer(renderType), matrixStack.last().pose())
		buffer.endBatch()
	}

	fun renderArea(matrixStack: MatrixStack, view: Vector3d, area: Area, colour: Color, renderSides: Boolean) {
		matrixStack.pushPose()
		matrixStack.translate(-view.x, -view.y, -view.z)

		val (r, g, b) = colour.getRGBColorComponents(null).let { Triple(it[0], it[1], it[2]) }
		val box = area.displayAabb.get()
		val xSize = box.xsize.toFloat()
		val ySize = box.ysize.toFloat()
		val zSize = box.zsize.toFloat()

		matrixStack.pushPose()
		matrixStack.translate(box.minX, box.minY, box.minZ)
		if (renderSides)
			renderSides(matrixStack, xSize, ySize, zSize, r, g, b)
		renderBoxEdges(matrixStack, xSize, ySize, zSize, r, g, b)
		matrixStack.popPose()

		val eyePos = mc.player!!.getEyePosition(mc.frameTime)
		val namePos = box.center.run { Vector3d(x, MathHelper.clamp(eyePos.y, box.minY + 0.5, box.maxY - 0.5), z) }
		renderName(matrixStack, area.name, namePos)

		matrixStack.popPose()
	}

	private fun renderSides(
		matrixStack: MatrixStack,
		xSize: Float,
		ySize: Float,
		zSize: Float,
		r: Float,
		g: Float,
		b: Float
	) {
		val a = ClientConfig.areaBoxAlpha().toFloat()
		if (a <= 0F)
			return

		renderWithType(renderTypeAreaSide, matrixStack) { matrix ->
			//North
			vertex(matrix, xSize, 0F, 0F).color(r, g, b, a).endVertex()
			vertex(matrix, xSize, ySize, 0F).color(r, g, b, a).endVertex()
			vertex(matrix, 0F, ySize, 0F).color(r, g, b, a).endVertex()
			vertex(matrix, 0F, 0F, 0F).color(r, g, b, a).endVertex()

			//South
			vertex(matrix, 0F, 0F, zSize).color(r, g, b, a).endVertex()
			vertex(matrix, 0F, ySize, zSize).color(r, g, b, a).endVertex()
			vertex(matrix, xSize, ySize, zSize).color(r, g, b, a).endVertex()
			vertex(matrix, xSize, 0F, zSize).color(r, g, b, a).endVertex()

			//East
			vertex(matrix, 0F, 0F, 0F).color(r, g, b, a).endVertex()
			vertex(matrix, 0F, ySize, 0F).color(r, g, b, a).endVertex()
			vertex(matrix, 0F, ySize, zSize).color(r, g, b, a).endVertex()
			vertex(matrix, 0F, 0F, zSize).color(r, g, b, a).endVertex()

			//West
			vertex(matrix, xSize, 0F, zSize).color(r, g, b, a).endVertex()
			vertex(matrix, xSize, ySize, zSize).color(r, g, b, a).endVertex()
			vertex(matrix, xSize, ySize, 0F).color(r, g, b, a).endVertex()
			vertex(matrix, xSize, 0F, 0F).color(r, g, b, a).endVertex()

			//Down
			vertex(matrix, 0F, 0F, zSize).color(r, g, b, a).endVertex()
			vertex(matrix, xSize, 0F, zSize).color(r, g, b, a).endVertex()
			vertex(matrix, xSize, 0F, 0F).color(r, g, b, a).endVertex()
			vertex(matrix, 0F, 0F, 0F).color(r, g, b, a).endVertex()

			//Up
			vertex(matrix, 0F, ySize, 0F).color(r, g, b, a).endVertex()
			vertex(matrix, xSize, ySize, 0F).color(r, g, b, a).endVertex()
			vertex(matrix, xSize, ySize, zSize).color(r, g, b, a).endVertex()
			vertex(matrix, 0F, ySize, zSize).color(r, g, b, a).endVertex()
		}
	}

	private fun renderBoxEdges(
		matrixStack: MatrixStack,
		xSize: Float,
		ySize: Float,
		zSize: Float,
		r: Float,
		g: Float,
		b: Float
	) {
		if (ClientConfig.areaBoxEdgeThickness() <= 0.0)
			return

		val minXminYminZ = Vector3f(0F, 0F, 0F)
		val minXminYmaxZ = Vector3f(0F, 0F, zSize)
		val minXmaxYminZ = Vector3f(0F, ySize, 0F)
		val maxXminYminZ = Vector3f(xSize, 0F, 0F)
		val minXmaxYmaxZ = Vector3f(0F, ySize, zSize)
		val maxXmaxYminZ = Vector3f(xSize, ySize, 0F)
		val maxXminYmaxZ = Vector3f(xSize, 0F, zSize)
		val maxXmaxYmaxZ = Vector3f(xSize, ySize, zSize)
		renderBoxEdgesForSide(matrixStack, UP, r, g, b, minXmaxYmaxZ, maxXmaxYmaxZ, maxXmaxYminZ, minXmaxYminZ)
		renderBoxEdgesForSide(matrixStack, DOWN, r, g, b, minXminYminZ, maxXminYminZ, maxXminYmaxZ, minXminYmaxZ)
		renderBoxEdgesForSide(matrixStack, NORTH, r, g, b, maxXminYminZ, minXminYminZ, minXmaxYminZ, maxXmaxYminZ)
		renderBoxEdgesForSide(matrixStack, SOUTH, r, g, b, minXminYmaxZ, maxXminYmaxZ, maxXmaxYmaxZ, minXmaxYmaxZ)
		renderBoxEdgesForSide(matrixStack, EAST, r, g, b, maxXminYmaxZ, maxXminYminZ, maxXmaxYminZ, maxXmaxYmaxZ)
		renderBoxEdgesForSide(matrixStack, WEST, r, g, b, minXminYminZ, minXminYmaxZ, minXmaxYmaxZ, minXmaxYminZ)
	}

	private fun renderBoxEdgesForSide(
		matrixStack: MatrixStack,
		side: Direction,
		r: Float,
		g: Float,
		b: Float,
		vararg corners: Vector3f
	) {
		val offsetByVertex = offsets.getValue(side)
		val thickness = ClientConfig.areaBoxEdgeThickness().toFloat()

		// Outer
		renderWithType(renderTypeAreaEdge, matrixStack) { matrix ->
			for (i in 0..4) {
				val actualI = if (i < 4) i else 0
				val triple: Triple<Vector3f, Vector3f, Vector3f> = offsetByVertex[actualI]
				var v = corners[actualI].copy().also { corner ->
					corner.add(triple.first.copy().also { it.mul(thickness) })
				}
				vertex(matrix, v.x(), v.y(), v.z()).color(r, g, b, 1F).endVertex()
				v = corners[actualI].copy().also { corner ->
					corner.add(triple.second.copy().also { it.mul(thickness) })
				}
				vertex(matrix, v.x(), v.y(), v.z()).color(r, g, b, 1F).endVertex()
			}
		}

		//Inner
		renderWithType(renderTypeAreaEdge, matrixStack) { matrix ->
			for (i in 0..4) {
				val actualI = if (i < 4) i else 0
				val triple: Triple<Vector3f, Vector3f, Vector3f> = offsetByVertex[actualI]
				var v = corners[actualI].copy().also { corner ->
					corner.add(triple.third.copy().also { it.mul(thickness) })
				}
				vertex(matrix, v.x(), v.y(), v.z()).color(r, g, b, 1F).endVertex()
				v = corners[actualI].copy().also { corner ->
					corner.add(triple.first.copy().also { it.mul(thickness) })
				}
				vertex(matrix, v.x(), v.y(), v.z()).color(r, g, b, 1F).endVertex()
			}
		}
	}

	private fun renderName(matrixStack: MatrixStack, name: String, pos: Vector3d) {
		matrixStack.pushPose()
		matrixStack.translate(pos.x, pos.y, pos.z)
		matrixStack.mulPose(mc.gameRenderer.mainCamera.rotation())
		val scale = 0.04F * ClientConfig.areaNameScale().toFloat()
		matrixStack.scale(-scale, -scale, scale)

		val fr = mc.font
		val width = -(fr.width(name) / 2).toFloat()
		val bgColour = (mc.options.getBackgroundOpacity(0.25F) * 255F).toInt() shl 24
		val buffer = mc.renderBuffers().bufferSource()
		fr.drawInBatch(
			StringTextComponent(name),
			width,
			0F,
			-1,
			false,
			matrixStack.last().pose(),
			buffer,
			false,
			bgColour,
			15728880
		)
		buffer.endBatch()
		matrixStack.popPose()
	}
}
