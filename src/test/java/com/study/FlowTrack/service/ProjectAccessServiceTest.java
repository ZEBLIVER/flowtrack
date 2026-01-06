package com.study.FlowTrack.service;

import com.study.FlowTrack.enums.ProjectRole;
import com.study.FlowTrack.exception.PermissionDeniedException;
import com.study.FlowTrack.exception.ResourceNotFoundException;
import com.study.FlowTrack.model.*;
import com.study.FlowTrack.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectAccessServiceTest {
    @InjectMocks
    private ProjectAccessService accessService;

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private ProjectMembershipRepository projectMembershipRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CommentRepository commentRepository;

    private Task task;
    private Project project;
    private ProjectMembership projectMembership;

    @BeforeEach
    void setUp() {
        task = new Task();
        project = new Project();
        projectMembership = new ProjectMembership();
    }

    @Test
    void getProjectEntityById_Success() {
        Long id = 1L;
        project.setId(id);

        when(projectRepository.findById(id)).thenReturn(Optional.of(project));

        Project result = accessService.getProjectEntityById(id);

        assertNotNull(result);
        assertEquals(1L,result.getId());
        verify(projectRepository).findById(id);
    }

    @Test
    void getProjectEntityById_ResourceNotFound() {
        Long id = 1L;

        when(projectRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> accessService.getProjectEntityById(id));
    }

    @Test
    void getProjectEntityByKey_Success() {
        String key = "key";
        project.setKey(key);
        project.setId(2L);

        when(projectRepository.findProjectByKey(key))
                .thenReturn(Optional.ofNullable(project));
        Project result = accessService.getProjectEntityByKey(key);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        verify(projectRepository).findProjectByKey(key);
    }

    @Test
    void getProjectEntityByKey_ResourceNotFound() {
        String key = "key";

        when(projectRepository.findProjectByKey(key)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> accessService.getProjectEntityByKey(key));
    }

    @Test
    void getUserById_Success() {
        User user = new User();
        user.setUserName("bob");
        Long id = 1L;
        user.setId(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        User responseUser = accessService.getUserById(id);

        assertNotNull(responseUser);
        assertEquals(1L,user.getId());
        verify(userRepository).findById(id);
    }

    @Test
    void getUserById_ResourceNotFound() {
        Long id = 1L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> accessService.getUserById(id));
    }

    @Test
    void getTaskByProjectKeyAndNumber_Success() {
        String projectKey = "key";
        Long taskNumber = 1L;

        task.setId(1L);

        when(projectRepository.findProjectByKey(projectKey))
                .thenReturn(Optional.of(project));
        when(taskRepository.findByProjectAndTaskNumber(project,taskNumber))
                .thenReturn(Optional.ofNullable(task));

        Task responseTask = accessService.getTaskByProjectKeyAndNumber(projectKey,taskNumber);

        assertNotNull(responseTask);
        assertEquals(1L, task.getId());
        verify(projectRepository).findProjectByKey(projectKey);
        verify(taskRepository).findByProjectAndTaskNumber(project,taskNumber);
    }

    @Test
    void getTaskByProjectKeyAndNumber_ResourceNotFound() {
        String projectKey = "key";
        Long taskNumber = 1L;

        when(projectRepository.findProjectByKey(projectKey))
                .thenReturn(Optional.of(project));
        when(taskRepository.findByProjectAndTaskNumber(project,taskNumber))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> accessService.getTaskByProjectKeyAndNumber(projectKey,taskNumber));
    }

    @Test
    void requireProjectNotViewer_WhenViewer_ThrowsException() {
        projectMembership.setProjectRole(ProjectRole.PROJECT_VIEWER);
        assertThrows(PermissionDeniedException.class,
                () -> accessService.requireProjectNotViewer(projectMembership));
    }

    @Test
    void requireProjectNotViewer_WhenAdmin_DoesNotThrow() {
        projectMembership.setProjectRole(ProjectRole.PROJECT_ADMIN);
        assertDoesNotThrow(() -> accessService.requireProjectNotViewer(projectMembership));
    }

    @Test
    void requireUserMembership_Success() {
        User requester = new User();
        projectMembership.setId(1L);
        when(projectMembershipRepository.findByUserAndProject(requester,project))
                .thenReturn(Optional.ofNullable(projectMembership));

        ProjectMembership result = accessService.requireUserMembership(requester,project);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(projectMembershipRepository).findByUserAndProject(requester,project);
    }

    @Test
    void requireUserMembership_NotFound_ThrowsException() {
        User user = new User();

        when(projectMembershipRepository.findByUserAndProject(user, project))
                .thenReturn(Optional.empty());

        assertThrows(PermissionDeniedException.class,
                () -> accessService.requireUserMembership(user, project));
    }

    @Test
    void requireProjectAdminOrProductManager_AdminSuccess() {
        projectMembership.setProjectRole(ProjectRole.PROJECT_ADMIN);
        assertDoesNotThrow(() -> accessService.requireProjectAdminOrProductManager(projectMembership));
    }

    @Test
    void requireProjectAdminOrProductManager_ProductManagerSuccess() {
        projectMembership.setProjectRole(ProjectRole.PROJECT_PRODUCT_MANAGER);
        assertDoesNotThrow(() -> accessService.requireProjectAdminOrProductManager(projectMembership));
    }

    @Test
    void requireProjectAdminOrProductManager_WhenViewer_ThrowsException() {
        projectMembership.setProjectRole(ProjectRole.PROJECT_VIEWER);
        assertThrows(PermissionDeniedException.class,
                () -> accessService.requireProjectAdminOrProductManager(projectMembership));
    }

    @Test
    void requireProjectAdminOrProductManager_WhenDeveloper_ThrowsException() {
        projectMembership.setProjectRole(ProjectRole.PROJECT_DEVELOPER);
        assertThrows(PermissionDeniedException.class,
                () -> accessService.requireProjectAdminOrProductManager(projectMembership));
    }


    @Test
    void requireProjectAdmin_Success() {
        projectMembership.setProjectRole(ProjectRole.PROJECT_ADMIN);
        assertDoesNotThrow(() -> accessService.requireProjectAdmin(projectMembership));
    }

    @Test
    void requireProjectAdmin_WhenProductManager_ThrowsException() {
        projectMembership.setProjectRole(ProjectRole.PROJECT_PRODUCT_MANAGER);
        assertThrows(PermissionDeniedException.class, () -> accessService.requireProjectAdmin(projectMembership));
    }

    @Test
    void requireProjectAdmin_WhenViewer_ThrowsException() {
        projectMembership.setProjectRole(ProjectRole.PROJECT_VIEWER);
        assertThrows(PermissionDeniedException.class, () -> accessService.requireProjectAdmin(projectMembership));
    }

    @Test
    void getCommentById_Success() {
        Long id = 1L;
        Comment comment = new Comment();
        comment.setId(1L);
        when(commentRepository.findById(id)).thenReturn(Optional.of(comment));
        Comment result = accessService.getCommentById(id);

        assertNotNull(result);
        assertEquals(1L,result.getId());
        verify(commentRepository).findById(id);
    }

    @Test
    void getCommentById_NotFound() {
        Long id = 99L;
        when(commentRepository.findById(id)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> accessService.getCommentById(id));

        assertEquals("Comment with id: 99 not found", ex.getMessage());
    }

    @Test
    void requireProjectAdminOrProductManagerOrCommentOwner_OwnerSuccess() {
        User author = new User();
        author.setId(10L);

        Comment comment = new Comment();
        comment.setAuthor(author);

        User requester = new User();
        requester.setId(10L);

        projectMembership.setUser(requester);
        projectMembership.setProjectRole(ProjectRole.PROJECT_DEVELOPER);

        assertDoesNotThrow(() -> accessService
                .requireProjectAdminOrProductManagerOrCommentOwner(projectMembership,comment));
    }

    @Test
    void requireProjectAdminOrProductManagerOrCommentOwner_AdminSuccess() {
        User author = new User();
        author.setId(111L);
        Comment comment = new Comment();
        comment.setAuthor(author);

        User admin = new User();
        admin.setId(1L);
        projectMembership.setUser(admin);
        projectMembership.setProjectRole(ProjectRole.PROJECT_ADMIN);

        assertDoesNotThrow(() -> accessService
                .requireProjectAdminOrProductManagerOrCommentOwner(projectMembership,comment));
    }

    @Test
    void requireProjectAdminOrProductManagerOrCommentOwner_ProductManagerSuccess() {
        User author = new User();
        author.setId(111L);
        Comment comment = new Comment();
        comment.setAuthor(author);

        User product = new User();
        product.setId(1L);
        projectMembership.setUser(product);
        projectMembership.setProjectRole(ProjectRole.PROJECT_PRODUCT_MANAGER);

        assertDoesNotThrow(() -> accessService
                .requireProjectAdminOrProductManagerOrCommentOwner(projectMembership,comment));
    }

    @Test
    void requireProjectAdminOrProductManagerOrCommentOwner_WhenNotOwnerAndNotPrivileged_ThrowsException() {
        User author = new User();
        author.setId(111L);
        Comment comment = new Comment();
        comment.setAuthor(author);

        User stranger = new User();
        stranger.setId(1L);
        projectMembership.setUser(stranger);
        projectMembership.setProjectRole(ProjectRole.PROJECT_DEVELOPER);

        assertThrows(PermissionDeniedException.class, () -> accessService
                .requireProjectAdminOrProductManagerOrCommentOwner(projectMembership,comment));
    }
}