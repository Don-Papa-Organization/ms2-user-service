import { Router } from "express";
import { ManejadorTokensController } from "../controllers/manejadorTokensController";

const router = Router();
const manejadorTokensController = new ManejadorTokensController();

router.get('/token/:token', manejadorTokensController.getByToken.bind(manejadorTokensController));
router.get('/usuario/:idUsuario', manejadorTokensController.getByUsuario.bind(manejadorTokensController));
router.get('/', manejadorTokensController.getAll.bind(manejadorTokensController));
router.get('/:id', manejadorTokensController.getById.bind(manejadorTokensController));
router.post('/', manejadorTokensController.create.bind(manejadorTokensController));
router.put('/:id', manejadorTokensController.update.bind(manejadorTokensController));
router.delete('/:id', manejadorTokensController.delete.bind(manejadorTokensController));

export default router;
