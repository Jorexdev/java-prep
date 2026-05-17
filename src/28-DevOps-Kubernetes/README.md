<div align="center">
  <a href="#"><img src="../../assets/modules/banner-28-devops-kubernetes-v1.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-concepto-v2.svg" width="100%" alt="// concepto"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**Kubernetes (K8s)** es un orquestador de contenedores open-source que automatiza el despliegue, escalado y gestión de aplicaciones en contenedores. Resuelve los problemas de operar Docker a escala.

**Arquitectura:**
```
Cluster
├── Control Plane (master)
│   ├── API Server          ← punto de entrada de toda comunicación
│   ├── Scheduler           ← asigna pods a nodos
│   ├── Controller Manager  ← mantiene el estado deseado
│   └── etcd                ← almacén de estado del cluster
└── Worker Nodes
    ├── kubelet             ← agente en cada nodo
    ├── kube-proxy          ← networking del nodo
    └── Container Runtime   ← Docker/containerd
```

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**Objetos principales:**

| Objeto | Descripción |
|---|---|
| **Pod** | Unidad mínima: 1+ contenedores que comparten red y almacenamiento |
| **Deployment** | Gestiona N réplicas de un Pod + rolling updates |
| **Service** | Expone Pods con una IP/DNS estable (ClusterIP/NodePort/LoadBalancer) |
| **ConfigMap** | Configuración no sensible (inyectada como env vars o archivos) |
| **Secret** | Configuración sensible (passwords, tokens) — base64 encoded |
| **HPA** | Horizontal Pod Autoscaler — escala réplicas según CPU/memoria |
| **Ingress** | Punto de entrada HTTP/S con routing por host/path |

**Comandos básicos:**
```bash
kubectl get pods                   # lista pods
kubectl describe pod <nombre>      # detalles
kubectl logs <pod>                 # logs del pod
kubectl apply -f deployment.yaml   # aplica manifiesto
kubectl scale deployment app --replicas=5
```

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

-  Alta disponibilidad: si un pod falla, Kubernetes lo reemplaza automáticamente.
- Escalado automático: HPA ajusta las réplicas según la carga.
- Rolling updates: despliegues sin downtime con rollback automático si falla.
- ConfigMap/Secret: configuración externalizada sin recompilar imágenes.

Este módulo es solo teoría — los manifiestos YAML se aplican en un cluster real.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
