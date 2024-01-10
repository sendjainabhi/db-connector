
# Steps to deploy custom accelerator in TAP with private git repo

```
Steps to deploy custom accelerator in TAP with private git repo

# create private git repo secret

kubectl create secret generic git-abhi-credentials \
    --namespace accelerator-system \
    --from-literal=username=<git user> \
    --from-literal=password=<git repo token>

# apply custom accelerator yaml file on tap k8 cluster
kubectl apply -f db-connector-accelerator.yaml 

# workload deployment steps
tanzu apps workload create db-connector \
--git-repo https://github.com/sendjainabhi/db-connector \
--git-branch main \
--type web \
--label app.kubernetes.io/part-of=db-connector \
--yes

```
