import { Router } from "express";
import { UserController } from "../controllers/userController";

const router = Router();
const userController = new UserController();


router.get('/correo/:correo', userController.getByEmail.bind(userController))
router.get('/tipo/:tipoUsuario', userController.getByUserType.bind(userController))
router.get('/activo/:activo', userController.getByActivo.bind(userController))

router.get('/', userController.getAll.bind(userController))
router.get('/:id', userController.getById.bind(userController))

router.post('/', userController.create.bind(userController))
router.put('/:id', userController.update.bind(userController))
router.delete('/:id', userController.delete.bind(userController))

export default router