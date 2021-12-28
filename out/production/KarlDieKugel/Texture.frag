#version 430 core
// Fragment Shader based on Basic.frag and BlinnPhongPointTex.frag
// Author: Karsten Lehn
// Version: 7.10.2018

// - Hanna Langenberg
// Version: 30.12.2020

// INPUT
in VS_OUT
{
	vec3 N;
	vec3 L;
	vec3 V;
	vec2 UV;
} fs_in;
// light
layout (location = 4) uniform vec4 lightSourceAmbient;
layout (location = 5) uniform vec4 lightSourceDiffuse;
layout (location = 6) uniform vec4 lightSourceSpecular;
// material
layout (location = 7) uniform vec4 materialAmbient;
layout (location = 8) uniform vec4 materialDiffuse;
layout (location = 9) uniform vec4 materialSpecular;
// texture
layout (binding = 0) uniform sampler2D tex;

// OUTPUT
out vec4 FragColor;

void main (void)
{
	vec3 ambient = vec3(materialAmbient) * vec3(lightSourceAmbient);
	vec3 diffuseAlbedo = vec3(materialDiffuse) * vec3(lightSourceDiffuse);
	vec3 specularAlbedo = vec3(materialSpecular) * vec3(lightSourceSpecular);

	vec3 N = normalize(fs_in.N);						// Normalize the incoming N, L and V vectors
	vec3 L = normalize(fs_in.L);
	vec3 V = normalize(fs_in.V);
	vec3 H = normalize(L + V);

	vec3 diffuse = max(dot(N, L), 0.0) * diffuseAlbedo;	// Compute the diffuse and specular components for each fragment
	vec3 specular = max(dot(N, H), 0.0) * specularAlbedo;

	FragColor = vec4(ambient + diffuse, 1.0) * texture(tex, fs_in.UV) + vec4(specular, 1.0);
}
