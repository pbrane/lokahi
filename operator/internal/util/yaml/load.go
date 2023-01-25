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

package yaml

import (
	"errors"
	"fmt"
	"github.com/OpenNMS-Cloud/opennms-operator/internal/model/values"
	"github.com/OpenNMS-Cloud/opennms-operator/internal/util/template"
	"k8s.io/apimachinery/pkg/util/yaml"
	"os"
	"strings"
)

type LoadTemplate struct {
	Filename   string
	Values     values.TemplateValues
	DecodeInto interface{}
}

// LoadYaml - loads a yaml from the given lt.Filename and templates the given values into it
func LoadYaml(lt LoadTemplate) error {
	cache := Cache()
	file, found := cache.Get(lt.Filename)
	if !found {
		loadedFile, err := loadFromFile(lt.Filename)
		if err != nil {
			return errors.New(fmt.Sprintf("%s: failed to load config: %v", lt.Filename, err))
		}
		cache.Set(lt.Filename, loadedFile)
		file = loadedFile
	}
	templatedConfig, err := template.TemplateConfig(file, lt.Values)
	if err != nil {
		return errors.New(fmt.Sprintf("%s: failed to template config: %v", lt.Filename, err))
	}
	err = decodeIntoObject(templatedConfig, lt.DecodeInto)
	if err != nil {
		return errors.New(fmt.Sprintf("%s: failed to unmarshal config: %v", lt.Filename, err))
	}
	return nil
}

// decodeIntoObject - decode a given yaml string into a give interface
func decodeIntoObject(yamlStr string, decodeInto interface{}) error {
	reader := strings.NewReader(yamlStr)
	return yaml.NewYAMLOrJSONDecoder(reader, 4096).Decode(decodeInto)
}

// loadFromFile - loads a given lt.Filename
func loadFromFile(filename string) (string, error) {
	file, err := os.ReadFile(filename)
	if err != nil {
		return "", err
	}
	return string(file), nil
}
