
def init():
  return "sh -c ./local-sample/run.sh"

k8s_context() 

init_complete = os.getenv( "INIT_COMPLETE" , default = "FALSE" ) 

if init_complete == "FALSE":
  os.putenv( "INIT_COMPLETE" , "TRUE" ) 
  local(init(), quiet=False)
  k8s_yaml('./operator/local-instance.yaml')

if init_complete == "TRUE":
  print("Test var: ", init_complete) 

# See for more info https://docs.tilt.dev/api.html#api.k8s_kind
# This is to do with the resource kind, not the Kind cluster manager. This
# updates the image field on the opennms crd for the api resource, will have to
# add one for each resource (ui, core, etc). May be better to just update the
# image directly. 
# TODO: The operator has a bug where it does not update yet the deployments and
# redeploys when this is changed, but it should and will be implemented when
# the fix is in place (FIXED, merged into develop, need to test). See the change below.
k8s_yaml('./operator/local-instance.yaml')
custom_build(
  'opennms/horizon-stream-ui',
  './run.sh $EXPECTED_REF',
  live_update=[
    sync('.', '/app')
  ]
)
k8s_kind('opennms', image_json_path='{.spec.ui.version}')
# Just apply the change directly to the 

# For importing images into kind cluster, see
# https://github.com/tilt-dev/kind-local for optimizing this process.

# Note: For testing the
# https://github.com/tilt-dev/tilt-example-java/tree/master/101-jib, I had to
# update the record-start-time.sh with the following:
#    
#    # Needed to install 'brew install coreutils' on Mac. And change the date to 
#    # gdate, had to remove the leading zero on startTimeNanos.
#    cat src/main/java/dev/tilt/example/IndexController.java | \
#        sed -e "s/startTimeSecs = .*;/startTimeSecs = $(gdate +%-s);/" | \
#        sed -e "s/startTimeNanos = .*;/startTimeNanos = $(gdate +%-N);/" > \
#        $tmpfile

# This temporary until the opennms operator can automatically update the
# deployment from a CRD change.
#local("kubectl -n opennms rollout restart deployment.apps/opennms-operator", quiet=False)

# Go example:
#docker_build('example-go-image', '.', dockerfile='deployments/Dockerfile')
#k8s_yaml('deployments/kubernetes.yaml')
# TODO: Need a way to update the image directly possibly.

# Should not need this, seeing that we have ingresses installed. They update
# automatically.
#k8s_resource('example-go', port_forwards=8000)

###### The following is what we should use.

#load('../Tiltfile', 'k8s_attach')

def k8s_attach(name, obj, namespace="", deps=[], live_update=None, image_selector="", container_selector="", **kwargs):
  """Attach to a kubernetes resource.

  Arguments:
  name: The name for the resource in the UI.
  obj: An object name in the form "kind/name", as you would express it to kubectl. e.g., deployment/my-deployment.
  namespace: The namespace of the object. If not specified, uses the default namespace.
  deps: When using live-update, you must also specify the files as a dependency.
  live_update: Live-update rules to copy files and run commands in the server whenever
    they change locally.
  image_selector: When using live-update, the image selector finds the container to update
    by comparing container image name.
  container_selector: When using live-update, the container selector finds the container to update
    by comparing container name.
  **kwargs: Arguments to pass to the underlying k8s_resource, including labels.
  """

  args = ["kubectl", "get", "-o=yaml", obj]
  if namespace:
    args.extend(["-n", namespace])

  deploy_kwargs={}
  if image_selector:
    deploy_kwargs["image_selector"] = image_selector
  if container_selector:
    deploy_kwargs["container_selector"] = container_selector

  k8s_custom_deploy(
    name,
    apply_cmd=args,
    delete_cmd=["echo", "Skipping delete. Object managed outside of tilt: %s" % obj],
    deps=deps,
    live_update=live_update,
    **deploy_kwargs)

  k8s_resource(
    name, **kwargs)

# NB: attach() is intended for attaching to existing
# deployments. Normally, you'd do the kubectl apply
# outside of the main
local('kubectl apply -f deployment.yaml')

k8s_attach(
  'opennms-ui',
  'deployment/opennms-ui',
  'local-instance',
  live_update=[
    sync('/Users/jaberry/Documents/contrib.opennms-horizon-stream/ui/src/components/Alarms/AlarmsTable.vue', '/app/src/components/Alarms/AlarmsTable.vue'),
  ],
  deps=['/Users/jaberry/Documents/contrib.opennms-horizon-stream/ui/'],
  port_forwards=[3000],
  image_selector='opennms/horizon-stream-ui:0.0.14')
