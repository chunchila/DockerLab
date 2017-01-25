package sample;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.SearchItem;
import com.github.dockerjava.core.DockerClientBuilder;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.extensions.DeploymentList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class Main extends Application {


    private String kubeUrl = "http://192.168.1.71:8080/r/projects/1a12/kubernetes";
    private String dockerUrl = "tcp://192.168.1.71:2375";


    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Docker Lab");


        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);
        vbox.setAlignment(Pos.CENTER);


        Text title = new Text("Docker Selection");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        vbox.getChildren().add(title);

        ListView<String> listViewDocker = new ListView<>();
        listViewDocker.getItems().add("Select Docker To Search ... ");
        listViewDocker.setMaxHeight(Control.USE_PREF_SIZE);
        listViewDocker.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        vbox.getChildren().add(listViewDocker);

        Label labelStatusDocker = new Label("Status Docker : ");
        vbox.getChildren().addAll(labelStatusDocker);


        HBox hBoxDocker = new HBox();
        hBoxDocker.setPadding(new Insets(10));
        hBoxDocker.setSpacing(8);
        hBoxDocker.setMaxWidth(Control.USE_PREF_SIZE);
        TextField textField = new TextField("Docker Search");
        Button btnGetDocker = new Button("Get Docker ");
        hBoxDocker.getChildren().addAll(textField, btnGetDocker);
        vbox.getChildren().add(hBoxDocker);


        ListView<String> listViewKube = new ListView<>();
        listViewKube.getItems().add("Select Pods To Search ... ");
        listViewKube.setMaxHeight(Control.USE_PREF_SIZE);
        listViewKube.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        vbox.getChildren().add(listViewKube);


        Label labelStatusKube = new Label("Status Kube : ");
        vbox.getChildren().addAll(labelStatusKube);


        HBox hBoxKube = new HBox();
        hBoxKube.setPadding(new Insets(10));
        hBoxKube.setSpacing(8);
        hBoxKube.setMaxWidth(Control.USE_PREF_SIZE);
        TextField textFieldKube = new TextField("Docker Search");
        Button btnDeleteKube = new Button("Delete Pod ");
        Button btnRefreshKube = new Button("Refresh");
        Button btnDeployKube = new Button("Add Deployment");
        hBoxKube.getChildren().addAll(textFieldKube, btnDeleteKube, btnRefreshKube, btnDeployKube);
        vbox.getChildren().add(hBoxKube);


        primaryStage.setScene(new Scene(vbox, 450, 600));
        primaryStage.show();


        // init the Components
        DockerClient dockerClient = dockerConnect(dockerUrl);
        KubernetesClient kubernetesClient = kubeConnect(kubeUrl);


        // Create my NameSpace
        HashMap<String, String> stringStringHashMap = new HashMap<>();


        final String nameSpaceName = InetAddress.getLocalHost().getHostName().toLowerCase();


        createNameSpace(kubernetesClient, nameSpaceName, stringStringHashMap);


        kubeFillListView(kubeGetDeployments(kubernetesClient, nameSpaceName), listViewKube);


        btnGetDocker.setOnAction(new EventHandler<ActionEvent>() {
                                     @Override
                                     public void handle(ActionEvent event) {
                                         List<SearchItem> searchItems = dockerSearch(dockerClient, textField.getText());

                                         if (listViewDocker.getItems().size() > 0) {
                                             listViewDocker.getItems().remove(0, listViewDocker.getItems().size() - 1);
                                         }

                                         listViewDocker.getItems().remove(0);
                                         for (SearchItem searchItem : searchItems) {
                                             listViewDocker.getItems().add(searchItem.getName());
                                         }
                                     }
                                 }

        );


        btnRefreshKube.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                clearListView(listViewKube);

                kubeFillListView(kubeGetDeployments(kubernetesClient, nameSpaceName), listViewKube);
            }
        });

        btnDeleteKube.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                ObservableList<String> selectedItems = listViewKube.getSelectionModel().getSelectedItems();

                for (String deploy : selectedItems) {
                    kubeDeleteDeploy(kubernetesClient, deploy, nameSpaceName);

                }
                clearListView(listViewKube);
                kubeFillListView(kubeGetDeployments(kubernetesClient, nameSpaceName), listViewKube);


            }
        });

        btnDeployKube.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                HashMap<String, String> stringMap = new HashMap<>();

                stringMap.put("moshe", "haim");

                String containerName = listViewDocker.getSelectionModel().getSelectedItem();


                int rand = new Random().nextInt(452345346);
                kubeDeploy(kubernetesClient, containerName.replace("/", "-") + "-" + rand, stringMap, containerName, nameSpaceName);
                clearListView(listViewKube);
                kubeFillListView(kubeGetDeployments(kubernetesClient, nameSpaceName), listViewKube);
            }

        });


    }

    private String createNameSpace(KubernetesClient kubernetesClient, String namespaceName, HashMap<String, String> labels) {


        namespaceName = namespaceName.toLowerCase();
        NamespaceList list = kubernetesClient.namespaces().list();

        for (Namespace namespace : list.getItems()) {

            if (namespace.getMetadata().getName().startsWith(namespaceName)) {
                System.out.println("NameSpace : " + namespaceName + " Exists");
                return namespaceName;
            }
        }

        Namespace namespace = new NamespaceBuilder().withNewMetadata().withName(namespaceName).addToLabels(labels).endMetadata().build();
        kubernetesClient.namespaces().create(namespace);
        return namespaceName;

    }

    private boolean kubeDeletePod(KubernetesClient kubernetesClient, String podName) {

        PodList podList = kubeGetPods(kubernetesClient);

        for (Pod pod : podList.getItems()) {
            if (pod.getMetadata().getName().startsWith(podName)) {

                kubernetesClient.pods().delete(pod);
                System.out.println("pod Deleted : " + pod.getMetadata().getName());
                return true;
            }

        }
        return false;

    }

    private boolean kubeDeleteDeploy(KubernetesClient kubernetesClient, String deployName, String nameSpace) {

        DeploymentList deploymentList = kubeGetDeployments(kubernetesClient, nameSpace);


        for (Deployment deployment : deploymentList.getItems()) {
            if (deployment.getMetadata().getName().startsWith(deployName)) {

                kubernetesClient.extensions().deployments().inNamespace(nameSpace).delete(deployment);
                System.out.println("Deployment Deleted : " + deployment.getMetadata().getName());
                return true;
            }

        }
        return false;

    }

    private boolean kubeFillListView(PodList podList, ListView listViewKube) {

        clearListView(listViewKube);
        for (Pod pod : podList.getItems()) {
            listViewKube.getItems().add(pod.getMetadata().getName());

        }
        return true;
    }

    private boolean kubeFillListView(DeploymentList deploymentList, ListView listViewKube) {

        clearListView(listViewKube);
        try {

            for (Deployment deployment : deploymentList.getItems()) {
                listViewKube.getItems().add(deployment.getMetadata().getName());

            }
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;

        }
    }


    private void kubeDeploy(KubernetesClient kubernetesClient, String deploymentName, Map<String, String> labelsMap, String deployContainerName, String nameSpaceName) {


        String containerName;
        containerName = deployContainerName.replace("/", "-");
        Deployment deployment = new DeploymentBuilder()
                .withNewMetadata()
                .withName(deploymentName)
                .endMetadata()
                .withNewSpec()
                .withReplicas(1)
                .withNewTemplate()
                .withNewMetadata()
                .addToLabels(labelsMap)
                .endMetadata()
                .withNewSpec()
                .addNewContainer()
                .withName(containerName)
                .withImage(deployContainerName)
                .addNewPort()
                .withContainerPort(80)
                .endPort()
                .endContainer()
                .endSpec()
                .endTemplate()
                .endSpec()
                .build();


        Deployment createdDeploy = kubernetesClient.extensions().deployments().inNamespace(nameSpaceName).create(deployment);
        System.out.println("deploy created " + createdDeploy.toString());

    }

    public static void main(String[] args) {
        launch(args);
    }


    private boolean clearListView(ListView listView) {


        for (int i = 0; i < listView.getItems().size(); i++) {


            listView.getItems().remove(i);
            return true;

        }
        return false;


    }


    private KubernetesClient kubeConnect(String kubeUrl) {


        /*Config config = new ConfigBuilder().withMasterUrl(kubeUrl).build();
        KubernetesClient kubernetesClient = new DefaultKubernetesClient(config);*/

        KubernetesClient kubernetesClient = new DefaultKubernetesClient(kubeUrl);

        return kubernetesClient;
    }

    private DeploymentList kubeGetDeployments(KubernetesClient kubernetesClient, String nameSpace) {

        return kubernetesClient.extensions().deployments().inNamespace(nameSpace).list();
    }

    private PodList kubeGetPods(KubernetesClient kubernetesClient) {

        return kubernetesClient.pods().list();

        /*for (Pod pod : podList.getItems()){
            System.out.println(pod.getMetadata().getName());

        }*/


    }

    private DockerClient dockerConnect(String dockerUrl) {

        return DockerClientBuilder.getInstance(dockerUrl).build();


    }

    private List<SearchItem> dockerSearch(DockerClient dockerClient, String dockerName) {

        return dockerClient.searchImagesCmd(dockerName).exec();

    }


}
