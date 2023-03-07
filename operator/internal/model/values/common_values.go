/*
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package values

type ServiceValues struct {
	Enabled            bool           `yaml:"Enabled" default:"True"`
	Path               string         `yaml:"Path"`
	Port               int            `yaml:"Port"`
	GrpcPort           int            `yaml:"GrpcPort"`
	ServiceName        string         `yaml:"ServiceName"`
	Image              string         `yaml:"Image"`
	ImagePullPolicy    string         `yaml:"ImagePullPolicy" default:"Always"`
	Replicas           int            `yaml:"Replicas" default:"1"`
	VolumeSize         string         `yaml:"VolumeSize"`
	InitContainerImage string         `yaml:"InitContainerImage"`
	Resources          ResourceValues `yaml:"Resources"`
    PrivateRepoEnabled bool           `yaml:"PrivateRepoEnabled"`
}

type ResourceValues struct {
	Limits   ResourceDefinition `yaml:"Limits"`
	Requests ResourceDefinition `yaml:"Requests"`
}

type ResourceDefinition struct {
	Cpu    string `yaml:"Cpu"`
	Memory string `yaml:"Memory"`
}
