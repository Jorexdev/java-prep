package devops.kubernetes;

public class Intro {
/*
    KUBERNETES (K8s) — Orquestación de contenedores

    ► ¿Qué es Kubernetes?
      Sistema open source de orquestación de contenedores desarrollado por Google.
      Automatiza el despliegue, escalado, balanceo de carga y gestión de
      aplicaciones en contenedores Docker.

      Problema que resuelve: cuando tienes decenas o cientos de contenedores,
      gestionarlos manualmente es inviable. K8s proporciona un plano de control
      para hacerlo de forma declarativa y automatizada.

    ► Arquitectura básica

      Cluster = conjunto de nodos (máquinas).

      Control Plane (nodo maestro):
        - API Server     → punto de entrada para todas las operaciones (REST API).
        - etcd           → base de datos clave-valor que almacena el estado del cluster.
        - Scheduler      → decide en qué nodo se ejecuta cada Pod.
        - Controller Mgr → mantiene el estado deseado (replica sets, endpoints...).

      Worker Nodes (nodos de trabajo):
        - kubelet        → agente que corre en cada nodo y gestiona los Pods.
        - kube-proxy     → gestiona las reglas de red del nodo.
        - Container RT   → runtime de contenedores (containerd, CRI-O...).

    ── RECURSOS FUNDAMENTALES ─────────────────────────────────────────────────

    ► Pod
      La unidad mínima desplegable en K8s. Contiene uno o más contenedores
      que comparten red y almacenamiento.

      Ejemplo:
        apiVersion: v1
        kind: Pod
        metadata:
          name: mi-app
        spec:
          containers:
            - name: app
              image: miapp:1.0
              ports:
                - containerPort: 8080

      ⚠ Los Pods son efímeros. No se usan directamente en producción;
        se gestionan a través de Deployments.

    ► Deployment
      Gestiona la creación y actualización de réplicas de un Pod.
      Permite rollouts y rollbacks automáticos.

      Ejemplo:
        apiVersion: apps/v1
        kind: Deployment
        metadata:
          name: mi-app
        spec:
          replicas: 3
          selector:
            matchLabels:
              app: mi-app
          template:
            metadata:
              labels:
                app: mi-app
            spec:
              containers:
                - name: app
                  image: miapp:1.0
                  ports:
                    - containerPort: 8080

      Comandos:
        kubectl apply -f deployment.yaml      → aplica el manifiesto
        kubectl get deployments               → lista deployments
        kubectl rollout status deployment/mi-app
        kubectl rollout undo deployment/mi-app → rollback

    ► Service
      Expone un conjunto de Pods como un endpoint de red estable.
      Los Pods tienen IPs efímeras; el Service abstrae eso.

      Tipos:
        ClusterIP   → acceso interno al cluster (por defecto).
        NodePort    → expone el servicio en un puerto del nodo (para dev/test).
        LoadBalancer → provee una IP externa a través del cloud provider.

      Ejemplo:
        apiVersion: v1
        kind: Service
        metadata:
          name: mi-app-svc
        spec:
          selector:
            app: mi-app
          ports:
            - port: 80
              targetPort: 8080
          type: ClusterIP

    ► ConfigMap
      Almacena configuración no sensible en pares clave-valor.
      Desacopla la configuración de la imagen del contenedor.

      Ejemplo:
        apiVersion: v1
        kind: ConfigMap
        metadata:
          name: app-config
        data:
          APP_ENV: production
          LOG_LEVEL: info

      Uso en el Pod:
        envFrom:
          - configMapRef:
              name: app-config

    ► Secret
      Igual que ConfigMap pero para datos sensibles (passwords, tokens, keys).
      Los valores se almacenan en base64 (no es cifrado real; usar con RBAC).

      Ejemplo:
        apiVersion: v1
        kind: Secret
        metadata:
          name: app-secrets
        type: Opaque
        data:
          DB_PASSWORD: cGFzc3dvcmQxMjM=   # base64("password123")

      Uso en el Pod:
        envFrom:
          - secretRef:
              name: app-secrets

    ── OTROS RECURSOS ÚTILES ──────────────────────────────────────────────────

    ► Namespace
      Agrupación lógica de recursos dentro del cluster.
      Permite separar entornos (dev, staging, prod) en el mismo cluster.

    ► HorizontalPodAutoscaler (HPA)
      Escala automáticamente el número de réplicas según CPU/memoria u otras métricas.

        kubectl autoscale deployment mi-app --cpu-percent=70 --min=2 --max=10

    ► Ingress
      Gestiona el acceso HTTP/HTTPS externo al cluster.
      Permite rutar tráfico a distintos Services por path o dominio.

        /api  → service-api
        /web  → service-frontend

    ► PersistentVolume / PersistentVolumeClaim
      Almacenamiento persistente desacoplado del ciclo de vida del Pod.
      PV = recurso de almacenamiento en el cluster.
      PVC = solicitud de almacenamiento por parte de un Pod.

    ── COMANDOS ESENCIALES ────────────────────────────────────────────────────

      kubectl get pods                          → lista pods
      kubectl get pods -n namespace             → en un namespace específico
      kubectl describe pod <nombre>             → detalle del pod
      kubectl logs <pod> -f                     → ver logs en tiempo real
      kubectl exec -it <pod> -- /bin/sh         → shell dentro del pod
      kubectl apply -f manifiesto.yaml          → aplica configuración
      kubectl delete -f manifiesto.yaml         → elimina recursos
      kubectl scale deployment mi-app --replicas=5
      kubectl get events --sort-by='.lastTimestamp'

    ► Preguntas típicas de entrevista
      - ¿Qué diferencia hay entre un Pod y un Deployment?
      - ¿Cuándo usarías ClusterIP vs LoadBalancer?
      - ¿Cómo protegerías un Secret en producción? (RBAC + cifrado etcd + Vault)
      - ¿Qué pasa si un Pod falla? ¿Y si falla un nodo?
      - ¿Qué es el liveness probe y el readiness probe?
      - ¿Cómo funciona el rolling update en un Deployment?
*/
}
